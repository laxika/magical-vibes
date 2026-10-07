package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurveyMechan.class, Forest.class, Island.class, GrizzlyBears.class,
        InvasionOfZendikar.class, AwakenedSkyclave.class})
class SurveyMechanTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage and makes the targeted player draw cards and gain life")
    void resolvesBothTargetGroups() {
        addCreatureReady(player1, new SurveyMechan());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player2, 10);
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(targetCreature.getId(), player2.getId()));
        harness.assertInGraveyard(player1, "Survey Mechan");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 13);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerHands.get(player2.getId()).subList(handSizeBefore,
                gd.playerHands.get(player2.getId()).size())).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("The same player may be targeted for damage and for drawing and gaining life")
    void samePlayerCanBeChosenForBothEffects() {
        harness.addToBattlefield(player1, new SurveyMechan());
        harness.setLibrary(player2, List.of(new SurveyMechan(), new SurveyMechan(), new SurveyMechan()));
        harness.setLife(player2, 10);
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Survey Mechan");
        harness.assertLife(player2, 10);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Repeated land names count once and opposing lands do not reduce the cost")
    void countsOnlyDistinctControlledLandNames() {
        harness.addToBattlefield(player1, new SurveyMechan());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setLibrary(player1, List.of(new SurveyMechan(), new SurveyMechan(), new SurveyMechan()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player2.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Survey Mechan");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Survey Mechan");
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("A sacrificed damage target does not stop the targeted player drawing and gaining life")
    void resolvesPlayerEffectsWhenSourceWasDamageTarget() {
        Permanent mechan = harness.addToBattlefieldAndReturn(player1, new SurveyMechan());
        harness.setLibrary(player2, List.of(new SurveyMechan(), new SurveyMechan(), new SurveyMechan()));
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(mechan.getId(), player2.getId()));
        harness.assertInGraveyard(player1, "Survey Mechan");
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @CardUsed({SurveyMechan.class, InvasionOfZendikar.class, AwakenedSkyclave.class})
    @DisplayName("Any target includes a battle")
    void canTargetBattleForDamage() {
        harness.addToBattlefield(player1, new SurveyMechan());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(battle.getId(), player1.getId()));

        harness.assertInGraveyard(player1, "Survey Mechan");
        assertThat(gd.stack).hasSize(1);
    }
}
