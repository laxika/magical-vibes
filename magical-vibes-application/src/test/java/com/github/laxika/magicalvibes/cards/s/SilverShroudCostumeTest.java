package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SilverShroudCostume.class, GrizzlyBears.class})
class SilverShroudCostumeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Silver Shroud Costume attaches it and grants temporary shroud")
    void enteringAttachesAndGrantsTemporaryShroud() {
        Permanent creature = addCreatureReady(player1);
        castSilverShroudCostume(creature);

        Permanent costume = findPermanent(player1, "Silver Shroud Costume");
        assertThat(costume.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Entry shroud expires at end of turn while unblockability remains")
    void entryShroudExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1);
        castSilverShroudCostume(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Equip {3} attaches Silver Shroud Costume to a creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent costume = addCostumeReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(costume.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Silver Shroud Costume cannot target an opponent's creature when entering")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2);
        harness.setHand(player1, List.of(new SilverShroudCostume()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash allows Silver Shroud Costume to be cast during an opponent's combat")
    void flashAllowsCastingDuringOpponentsCombat() {
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new SilverShroudCostume()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castArtifact(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private void castSilverShroudCostume(Permanent target) {
        harness.setHand(player1, List.of(new SilverShroudCostume()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addCostumeReady(Player player) {
        Permanent costume = new Permanent(new SilverShroudCostume());
        costume.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(costume);
        return costume;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
