package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
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
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SoundTheTrumpets(), new SerraAngel()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

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
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SoundTheTrumpets(), new Forest()));
        harness.setLibrary(player2, List.of(new SerraAngel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

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
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(new SoundTheTrumpets()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertInGraveyard(player2, "Sound the Trumpets");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }
}
