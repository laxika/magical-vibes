package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfHospitality.class, Divination.class, GrizzlyBears.class})
class CurseOfHospitalityTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures attacking the enchanted player have trample")
    void attackingCreaturesHaveTrample() {
        placeCurseOnPlayer2();
        Permanent attacker = addAttacker(player1, player2);
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addAttacker(player2, player1);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage exiles the damaged player's top card face up for the creature controller until end of turn")
    void combatDamageGrantsEndOfTurnPlayPermission() {
        Permanent curse = placeCurseOnPlayer2();
        addAttacker(player1, player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isFalse();
        assertThat(entry.ownerId()).isEqualTo(player2.getId());
        assertThat(entry.sourcePermanentId()).isNull();
        assertThat(entry.exilerId()).isNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).doesNotContain(topCard.getId());
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The creature controller can cast the exiled spell using mana of any color")
    void creatureControllerCanCastExiledSpellWithAnyMana() {
        placeCurseOnPlayer2();
        addAttacker(player1, player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBeforeCast = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeCast + 2);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    private Permanent placeCurseOnPlayer2() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfHospitality());
        curse.setAttachedTo(player2.getId());
        return curse;
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player attacker,
                                  com.github.laxika.magicalvibes.model.Player defender) {
        Permanent creature = addCreatureReady(attacker, new GrizzlyBears());
        creature.setAttacking(true);
        creature.setAttackTarget(defender.getId());
        return creature;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
