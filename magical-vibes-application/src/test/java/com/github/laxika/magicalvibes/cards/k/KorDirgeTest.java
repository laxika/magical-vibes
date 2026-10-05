package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.n.NeedlepeakSpider;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.ShivanMeteor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorDirge.class, NeedlepeakSpider.class, ProdigalPyromancer.class, ShivanMeteor.class})
class KorDirgeTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects all damage from the chosen source to the other target creature")
    void redirectsDamageToOtherTargetCreature() {
        Permanent protectedCreature = addCreatureReady(player1, new NeedlepeakSpider());
        Permanent redirectCreature = addCreatureReady(player2, new NeedlepeakSpider());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        castKorDirge(protectedCreature, redirectCreature);

        harness.handlePermanentChosen(player1, pyromancer.getId());
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(redirectCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage from another source still reaches the protected creature")
    void doesNotRedirectDamageFromAnotherSource() {
        Permanent protectedCreature = addCreatureReady(player1, new NeedlepeakSpider());
        Permanent redirectCreature = addCreatureReady(player2, new NeedlepeakSpider());
        Permanent chosenSource = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent otherSource = addCreatureReady(player1, new ProdigalPyromancer());
        castKorDirge(protectedCreature, redirectCreature);

        harness.handlePermanentChosen(player1, chosenSource.getId());
        harness.activateAbility(player1, indexOf(player1, otherSource), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(redirectCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Redirects multiple damage events from the chosen source this turn")
    void redirectsMultipleDamageEventsFromChosenSource() {
        Permanent protectedCreature = addCreatureReady(player1, new NeedlepeakSpider());
        Permanent redirectCreature = addCreatureReady(player2, new NeedlepeakSpider());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        castKorDirge(protectedCreature, redirectCreature);

        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, protectedCreature.getId());
        harness.passBothPriorities();
        pyromancer.untap();
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(redirectCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Redirects combat damage from the chosen source to the other target creature")
    void redirectsCombatDamage() {
        Permanent protectedCreature = addCreatureReady(player1, new NeedlepeakSpider());
        Permanent redirectCreature = addCreatureReady(player2, new NeedlepeakSpider());
        Permanent attacker = addCreatureReady(player2, new NeedlepeakSpider());
        castKorDirge(protectedCreature, redirectCreature);

        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, protectedCreature), indexOf(player2, attacker))));
        resolveCombat(player2);

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(redirectCreature.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("A spell on the stack is offered as a source choice")
    void offersSpellOnStackAsSource() {
        Permanent protectedCreature = addCreatureReady(player1, new NeedlepeakSpider());
        Permanent redirectCreature = addCreatureReady(player2, new NeedlepeakSpider());
        ShivanMeteor meteor = new ShivanMeteor();

        harness.setHand(player1, List.of(meteor));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, protectedCreature.getId());
        castKorDirge(protectedCreature, redirectCreature);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(meteor.getId());
    }

    @Test
    @DisplayName("Requires two different creature targets")
    void requiresDifferentCreatureTargets() {
        Permanent creature = addCreatureReady(player1, new NeedlepeakSpider());
        harness.setHand(player1, List.of(new KorDirge()));
        addCastMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires the protected target to be a creature you control")
    void requiresProtectedTargetToBeControlledByCaster() {
        Permanent opponentCreature = addCreatureReady(player2, new NeedlepeakSpider());
        Permanent ownCreature = addCreatureReady(player1, new NeedlepeakSpider());
        harness.setHand(player1, List.of(new KorDirge()));
        addCastMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(opponentCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Redirected damage is redirected again by another Kor Dirge")
    void chainsRedirectionThroughAnotherProtectedCreature() {
        Permanent firstCreature = addCreatureReady(player1, new NeedlepeakSpider());
        Permanent secondCreature = addCreatureReady(player1, new NeedlepeakSpider());
        Permanent finalCreature = addCreatureReady(player2, new NeedlepeakSpider());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        castKorDirge(firstCreature, secondCreature);
        harness.handlePermanentChosen(player1, pyromancer.getId());
        castKorDirge(secondCreature, finalCreature);
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, firstCreature.getId());
        harness.passBothPriorities();

        assertThat(firstCreature.getMarkedDamage()).isZero();
        assertThat(secondCreature.getMarkedDamage()).isZero();
        assertThat(finalCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Redirects damage from a chosen spell on the stack")
    void redirectsDamageFromSpellOnStack() {
        Permanent protectedCreature = addCreatureReady(player1, new NeedlepeakSpider());
        Permanent redirectCreature = addCreatureReady(player2, new NeedlepeakSpider());
        ShivanMeteor meteor = new ShivanMeteor();
        harness.setHand(player1, List.of(meteor));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, protectedCreature.getId());
        castKorDirge(protectedCreature, redirectCreature);

        harness.handlePermanentChosen(player1, meteor.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(redirectCreature);
        harness.assertInGraveyard(player2, "Needlepeak Spider");
    }

    @Test
    @DisplayName("Does not redirect damage after the destination leaves the battlefield")
    void damageReachesProtectedCreatureWhenDestinationHasLeft() {
        Permanent protectedCreature = addCreatureReady(player1, new NeedlepeakSpider());
        Permanent redirectCreature = addCreatureReady(player2, new ProdigalPyromancer());
        Permanent chosenSource = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent otherSource = addCreatureReady(player1, new ProdigalPyromancer());
        castKorDirge(protectedCreature, redirectCreature);
        harness.handlePermanentChosen(player1, chosenSource.getId());

        harness.activateAbility(player1, indexOf(player1, otherSource), null, redirectCreature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(redirectCreature);
        harness.activateAbility(player1, indexOf(player1, chosenSource), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Redirection expires when the turn ends")
    void redirectionExpiresAtEndOfTurn() {
        Permanent protectedCreature = addCreatureReady(player1, new NeedlepeakSpider());
        Permanent redirectCreature = addCreatureReady(player2, new NeedlepeakSpider());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        castKorDirge(protectedCreature, redirectCreature);
        harness.handlePermanentChosen(player1, pyromancer.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(redirectCreature.getMarkedDamage()).isZero();
    }

    private void castKorDirge(Permanent protectedCreature, Permanent redirectCreature) {
        harness.setHand(player1, List.of(new KorDirge()));
        addCastMana();
        harness.castAndResolveInstant(player1, 0, List.of(protectedCreature.getId(), redirectCreature.getId()));
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
