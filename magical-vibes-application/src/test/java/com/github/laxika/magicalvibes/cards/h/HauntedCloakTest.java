package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HauntedCloak.class, DevilthornFox.class})
class HauntedCloakTest extends BaseCardTest {

    @Test
    void equippedCreatureHasVigilanceTrampleAndHaste() {
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void creatureLosesGrantedKeywordsWhenCloakBecomesUnattached() {
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        cloak.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void equipAbilityAttachesCloakToTargetCreature() {
        Permanent cloak = addCloakReady(player1);
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cloak.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reEquippingMovesAllThreeKeywords() {
        Permanent cloak = addCloakReady(player1);
        Permanent first = addCreatureReady(player1, new DevilthornFox());
        Permanent second = addCreatureReady(player1, new DevilthornFox());
        cloak.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        assertThat(cloak.getAttachedTo()).isEqualTo(first.getId());
        harness.passBothPriorities();

        assertThat(cloak.getAttachedTo()).isEqualTo(second.getId());
        for (Keyword keyword : List.of(Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.HASTE)) {
            assertThat(gqs.hasKeyword(gd, first, keyword)).isFalse();
            assertThat(gqs.hasKeyword(gd, second, keyword)).isTrue();
        }
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        addCloakReady(player1);
        Permanent creature = addCreatureReady(player2, new DevilthornFox());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        addCloakReady(player1);
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void failedReEquipLeavesPreviousCreatureEquipped() {
        Permanent cloak = addCloakReady(player1);
        Permanent first = addCreatureReady(player1, new DevilthornFox());
        Permanent second = addCreatureReady(player1, new DevilthornFox());
        cloak.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());

        harness.passBothPriorities();

        assertThat(cloak.getAttachedTo()).isEqualTo(first.getId());
        for (Keyword keyword : List.of(Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.HASTE)) {
            assertThat(gqs.hasKeyword(gd, first, keyword)).isTrue();
        }
    }

    @Test
    void newlyEnteredEquippedCreatureAttacksUntappedAndTramplesOverBlocker() {
        addCloakReady(player1);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        attacker.setSummoningSick(true);
        Permanent blocker = addCreatureReady(player2, new DevilthornFox());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(1));
        assertThat(attacker.isTapped()).isFalse();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 1, Map.of(blocker.getId(), 1, player2.getId(), 2));

        harness.assertLife(player2, 18);
    }

    private Permanent addCloakReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new HauntedCloak());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
