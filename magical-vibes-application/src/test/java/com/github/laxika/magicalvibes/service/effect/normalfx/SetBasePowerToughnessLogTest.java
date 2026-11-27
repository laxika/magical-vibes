package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** The log lines of a one-shot base power and toughness setter that sets only one of the two values. */
@CardUsed({GrizzlyBears.class})
class SetBasePowerToughnessLogTest extends BaseCardTest {

    @Test
    @DisplayName("A setter of the opponents' creatures that sets only toughness doesn't log a null power")
    void opponentsCreaturesLogOnlyTheSideSet() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(setter(Subjects.creaturesYourOpponentsControl())));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(line -> line.contains("null"));
        assertThat(gameLogContains("base toughness of 1 opponent creature(s) to 1")).isTrue();
    }

    @Test
    @DisplayName("A setter of a set of creatures that sets only toughness doesn't log a null power")
    void aSetOfCreaturesLogsOnlyTheSideSet() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(setter(Subjects.creaturesYouControl())));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(line -> line.contains("null"));
        assertThat(gameLogContains("base toughness of 1 creature(s) to 1")).isTrue();
    }

    private static Card setter(com.github.laxika.magicalvibes.model.effect.PermanentSubject subject) {
        Card card = new Card();
        card.setName("Setter");
        card.setType(CardType.INSTANT);
        card.setManaCost("{U}");
        card.setColor(CardColor.BLUE);
        card.addSpellEffect((CardEffect) new SetBasePowerToughnessEffect(null, 1, subject));
        return card;
    }
}
