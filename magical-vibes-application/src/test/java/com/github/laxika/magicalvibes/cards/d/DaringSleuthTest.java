package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.i.IzzetCluestone;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaringSleuth.class, GrizzlyBears.class, HolyDay.class, IzzetCluestone.class})
class DaringSleuthTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms when its controller sacrifices a Clue")
    void transformsWhenControllerSacrificesClue() {
        Permanent sleuth = addReadySleuth();

        sacrificeClue(player1);

        assertThat(sleuth.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform when its controller sacrifices a non-Clue permanent")
    void doesNotTransformForNonClueSacrifice() {
        Permanent sleuth = addReadySleuth();
        Permanent cluestone = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        cluestone.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cluestone), 1, null, null);
        harness.passBothPriorities();

        assertThat(sleuth.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Bearer investigates after dealing combat damage to a player")
    void bearerInvestigatesOnCombatDamage() {
        Permanent sleuth = addReadySleuth();
        sacrificeClue(player1);

        sleuth.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Bearer's prowess boosts it for a noncreature spell")
    void bearerHasProwess() {
        Permanent sleuth = addReadySleuth();
        sacrificeClue(player1);

        int powerBefore = gqs.getEffectivePower(gd, sleuth);
        int toughnessBefore = gqs.getEffectiveToughness(gd, sleuth);
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, sleuth)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, sleuth)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("An opponent sacrificing a Clue does not transform Daring Sleuth")
    void opponentClueSacrificeDoesNotTransform() {
        Permanent sleuth = addReadySleuth();

        sacrificeClue(player2);

        assertThat(sleuth.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Two pending Clue sacrifice triggers transform Daring Sleuth only once")
    void pendingTransformTriggersDoNotTransformBack() {
        Permanent sleuth = addReadySleuth();
        addClueToken(player1);
        addClueToken(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DaringSleuth(), new DaringSleuth()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Permanent firstClue = findPermanents(player1, "Clue").getFirst();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(firstClue), null, null);
        harness.ensurePriority(player1);
        Permanent secondClue = findPermanents(player1, "Clue").getFirst();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(secondClue), null, null);

        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(sleuth.isTransformed()).isTrue();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Sacrificing a Clue after transformation does not transform Bearer back")
    void bearerDoesNotTransformForClueSacrifice() {
        Permanent sleuth = addReadySleuth();
        sacrificeClue(player1);

        sacrificeClue(player1);

        assertThat(sleuth.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Daring Sleuth does not have its back face's prowess")
    void frontFaceDoesNotHaveProwess() {
        Permanent sleuth = addReadySleuth();
        int powerBefore = gqs.getEffectivePower(gd, sleuth);
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, sleuth)).isEqualTo(powerBefore);
        assertThat(sleuth.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("A creature spell does not trigger Bearer's prowess")
    void creatureSpellDoesNotTriggerProwess() {
        Permanent sleuth = addReadySleuth();
        sacrificeClue(player1);
        int powerBefore = gqs.getEffectivePower(gd, sleuth);
        harness.setHand(player1, List.of(new DaringSleuth()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sleuth)).isEqualTo(powerBefore);
    }

    @Test
    @DisplayName("Bearer does not investigate when its combat damage is prevented")
    void preventedCombatDamageDoesNotInvestigate() {
        Permanent sleuth = addReadySleuth();
        sacrificeClue(player1);
        harness.setHand(player2, List.of(new HolyDay()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0);

        sleuth.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertLife(player2, 20);
    }

    private Permanent addReadySleuth() {
        return addCreatureReady(player1, new DaringSleuth());
    }

    private void sacrificeClue(Player player) {
        addClueToken(player);
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        Permanent clue = findPermanent(player, "Clue");
        harness.setLibrary(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.COLORLESS, 2);

        harness.activateAbility(player, battlefield.indexOf(clue), null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void addClueToken(Player player) {
        Card clueCard = new Card();
        clueCard.setName("Clue");
        clueCard.setType(CardType.ARTIFACT);
        clueCard.setManaCost("");
        clueCard.setToken(true);
        clueCard.setColor(null);
        clueCard.setSubtypes(List.of(CardSubtype.CLUE));
        clueCard.addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this token: Draw a card."
        ));
        Permanent clue = harness.addToBattlefieldAndReturn(player, clueCard);
        clue.setSummoningSick(false);
    }
}
