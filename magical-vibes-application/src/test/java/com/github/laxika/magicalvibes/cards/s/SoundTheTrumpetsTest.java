package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoundTheTrumpets.class, Forest.class, GrizzlyBears.class, SerraAngel.class})
class SoundTheTrumpetsTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a cheap spell and recruits after discarding a nonland card")
    void countersCheapSpellAndRecruitsAfterNonlandDiscard() {
        GrizzlyBears target = new GrizzlyBears();

        harness.setHand(player2, List.of(new SoundTheTrumpets(), new SerraAngel()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player2, 0);
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }

    @Test
    @DisplayName("Counters a cheap spell without recruiting after discarding a land card")
    void countersCheapSpellWithoutRecruitingAfterLandDiscard() {
        GrizzlyBears target = new GrizzlyBears();

        harness.setHand(player2, List.of(new SoundTheTrumpets(), new Forest()));
        harness.setLibrary(player2, List.of(new SerraAngel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player2, 0);
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }

    @Test
    @DisplayName("Counters an expensive spell without recruiting")
    void countersExpensiveSpellWithoutRecruiting() {
        SerraAngel target = new SerraAngel();

        harness.setHand(player2, List.of(new SoundTheTrumpets()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, target, "{3}{W}{W}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertInGraveyard(player2, "Sound the Trumpets");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }

    @Test
    @DisplayName("The target is countered before the recruit discard choice")
    void countersBeforeRecruiting() {
        GrizzlyBears target = beginCheapSpellRecruit();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(target.getId()));
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Recruit creates its token during the spell resolution")
    void createsTokenWithoutAnotherPriorityRound() {
        beginCheapSpellRecruit();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Recruit creates a Human Soldier")
    void createsHumanSoldier() {
        beginCheapSpellRecruit();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(token -> assertThat(token.getCard().getSubtypes())
                        .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER));
    }

    private GrizzlyBears beginCheapSpellRecruit() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player2, List.of(new SoundTheTrumpets(), new SerraAngel()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        return target;
    }
}
