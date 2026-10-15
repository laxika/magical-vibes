package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BaboonSpirit;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MechanicalGlider.class, BaboonSpirit.class})
class MechanicalGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Mechanical Glider attaches it to a creature you control and grants flying")
    void enteringAttachesAndGrantsFlying() {
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new MechanicalGlider()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent glider = findPermanent(player1, "Mechanical Glider");
        assertThat(glider.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equip moves Mechanical Glider and its flying grant to another creature")
    void equipMovesGliderToAnotherCreature() {
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new MechanicalGlider());
        glider.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, findGliderIndex(player1), null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(glider.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Mechanical Glider cannot target an opponent's creature when entering")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2);
        harness.setHand(player1, List.of(new MechanicalGlider()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Mechanical Glider can enter without any creatures to attach to")
    void entersWithoutCreatures() {
        harness.setHand(player1, List.of(new MechanicalGlider()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Mechanical Glider").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target a creature controlled by the opponent")
    void equipCannotTargetOpponentsCreature() {
        Permanent creature = addCreatureReady(player2);
        harness.addToBattlefield(player1, new MechanicalGlider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipCannotBeActivatedDuringCombat() {
        Permanent creature = addCreatureReady(player1);
        harness.addToBattlefield(player1, new MechanicalGlider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The attachment target can be chosen after Mechanical Glider enters")
    void choosesAttachmentTargetAfterEntering() {
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new MechanicalGlider()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent glider = findPermanent(player1, "Mechanical Glider");
        assertThat(glider.getAttachedTo()).isNull();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(glider.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }
    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new BaboonSpirit());
    }

    private int findGliderIndex(Player player) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard() instanceof MechanicalGlider) {
                return i;
            }
        }
        throw new AssertionError("Mechanical Glider not found");
    }
}
