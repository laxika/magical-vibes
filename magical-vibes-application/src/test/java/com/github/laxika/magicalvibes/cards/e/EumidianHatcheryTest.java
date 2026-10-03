package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({EumidianHatchery.class, StoneRain.class})
class EumidianHatcheryTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability pays life, adds black mana, and puts a hatchling counter on the land")
    void manaAbilityPaysLifeAddsManaAndPutsCounter() {
        Permanent hatchery = addReadyHatchery();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(hatchery.getCounterCount(CounterType.HATCHLING)).isEqualTo(1);
        assertThat(hatchery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates one flying black Insect for each hatchling counter when it leaves the battlefield")
    void createsInsectsForHatchlingCountersWhenDestroyed() {
        Permanent hatchery = harness.addToBattlefieldAndReturn(player1, new EumidianHatchery());
        hatchery.setCounterCount(CounterType.HATCHLING, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new StoneRain()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castSorcery(player2, 0, hatchery.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Insect")).hasSize(2)
                .allMatch(token -> token.getCard().getPower() == 1
                        && token.getCard().getToughness() == 1
                        && token.getCard().getColors().contains(CardColor.BLACK)
                        && token.getCard().getSubtypes().contains(CardSubtype.INSECT)
                        && token.getCard().getKeywords().contains(Keyword.FLYING));
    }

    private Permanent addReadyHatchery() {
        Permanent hatchery = new Permanent(new EumidianHatchery());
        hatchery.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hatchery);
        return hatchery;
    }
}
