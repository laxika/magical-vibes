package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
import com.github.laxika.magicalvibes.cards.l.Lightform;
import com.github.laxika.magicalvibes.cards.m.MarduScout;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattleBrawler.class, MarduScout.class, ArashinCleric.class, FeralKrushok.class,
        BatheInDragonfire.class, Lightform.class})
class BattleBrawlerTest extends BaseCardTest {

    @Test
    void getsBonusAndFirstStrikeWithRedPermanent() {
        harness.addToBattlefield(player1, new BattleBrawler());
        harness.addToBattlefield(player1, new MarduScout());

        assertBattleBrawlerHasBonus();
    }

    @Test
    void getsBonusAndFirstStrikeWithWhitePermanent() {
        harness.addToBattlefield(player1, new BattleBrawler());
        harness.addToBattlefield(player1, new ArashinCleric());

        assertBattleBrawlerHasBonus();
    }

    @Test
    void doesNotGetBonusFromOpponentsPermanent() {
        harness.addToBattlefield(player1, new BattleBrawler());
        harness.addToBattlefield(player2, new ArashinCleric());

        GameData gd = harness.getGameData();
        Permanent brawler = findPermanent(player1, "Battle Brawler");

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void doesNotGetBonusWithoutRedOrWhitePermanent() {
        harness.addToBattlefield(player1, new BattleBrawler());
        harness.addToBattlefield(player1, new FeralKrushok());

        GameData gd = harness.getGameData();
        Permanent brawler = findPermanent(player1, "Battle Brawler");

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void gainsBonusWhenAWhiteNoncreaturePermanentEnters() {
        harness.addToBattlefield(player1, new BattleBrawler());
        Permanent brawler = findPermanent(player1, "Battle Brawler");
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.FIRST_STRIKE)).isFalse();

        harness.setLibrary(player1, List.of(new FeralKrushok()));
        harness.setHand(player1, List.of(new Lightform()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Lightform");
        assertBattleBrawlerHasBonus();
    }

    @Test
    void bonusDoesNotStackWithMultipleQualifyingPermanents() {
        harness.addToBattlefield(player1, new BattleBrawler());
        harness.addToBattlefield(player1, new MarduScout());
        harness.addToBattlefield(player1, new ArashinCleric());

        assertBattleBrawlerHasBonus();
    }

    @Test
    void losesBonusWhenLastQualifyingPermanentDies() {
        harness.addToBattlefield(player1, new BattleBrawler());
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new MarduScout());
        assertBattleBrawlerHasBonus();

        harness.setHand(player1, List.of(new BatheInDragonfire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, scout.getId());

        harness.assertInGraveyard(player1, "Mardu Scout");
        Permanent brawler = findPermanent(player1, "Battle Brawler");
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void retainsBonusWhileAnotherQualifyingPermanentRemains() {
        harness.addToBattlefield(player1, new BattleBrawler());
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new MarduScout());
        harness.addToBattlefield(player1, new ArashinCleric());

        harness.setHand(player1, List.of(new BatheInDragonfire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, scout.getId());

        harness.assertInGraveyard(player1, "Mardu Scout");
        assertBattleBrawlerHasBonus();
    }

    private void assertBattleBrawlerHasBonus() {
        GameData gd = harness.getGameData();
        Permanent brawler = findPermanent(player1, "Battle Brawler");

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.FIRST_STRIKE)).isTrue();
    }
}
