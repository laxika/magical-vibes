package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
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

@CardUsed({DevotedPaladin.class, GrizzlyBears.class, DwarfholdChampion.class, PowerWordKill.class})
class DevotedPaladinTest extends BaseCardTest {

    private void castPaladin() {
        harness.castFromHand(player1, new DevotedPaladin(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB boosts and grants vigilance to creatures you control")
    void etbBoostsAndGrantsVigilance() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castPaladin();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        Permanent paladin = findPermanent(player1, "Devoted Paladin");
        assertThat(paladin.getEffectivePower()).isEqualTo(5);
        assertThat(paladin.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("ETB does not affect an opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castPaladin();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("ETB boost and vigilance wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castPaladin();

        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves do not receive the bonus")
    void laterCreaturesDoNotReceiveBonus() {
        castPaladin();

        harness.castFromHand(player1, new DwarfholdChampion(), "{1}{W}");
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Dwarfhold Champion");
        assertThat(champion.getEffectivePower()).isEqualTo(3);
        assertThat(champion.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, champion, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The trigger still boosts creatures when the Paladin is destroyed in response")
    void triggerResolvesWithoutSource() {
        harness.addToBattlefield(player1, new DwarfholdChampion());
        harness.castFromHand(player1, new DevotedPaladin(), "{4}{W}");
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Devoted Paladin");
        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, paladin.getId());
        harness.assertInGraveyard(player1, "Devoted Paladin");
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Dwarfhold Champion");
        assertThat(champion.getEffectivePower()).isEqualTo(4);
        assertThat(champion.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, champion, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Multiple Paladin triggers stack their boosts")
    void multipleTriggersStack() {
        harness.addToBattlefield(player1, new DwarfholdChampion());
        castPaladin();
        castPaladin();

        Permanent champion = findPermanent(player1, "Dwarfhold Champion");
        assertThat(champion.getEffectivePower()).isEqualTo(5);
        assertThat(champion.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, champion, Keyword.VIGILANCE)).isTrue();
    }
}
