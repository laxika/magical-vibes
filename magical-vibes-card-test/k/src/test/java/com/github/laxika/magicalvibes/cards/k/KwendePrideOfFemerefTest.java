package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Dub;
import com.github.laxika.magicalvibes.cards.s.SteelLeafChampion;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KwendePrideOfFemeref.class, BenalishKnight.class, GrizzlyBears.class,
        Dub.class, SteelLeafChampion.class})
class KwendePrideOfFemerefTest extends BaseCardTest {

    @Test
    @DisplayName("Own creature with first strike gains double strike")
    void ownFirstStrikeCreatureGainsDoubleStrike() {
        harness.addToBattlefield(player1, new KwendePrideOfFemeref());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());

        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Own creature without first strike does not gain double strike")
    void creatureWithoutFirstStrikeDoesNotGainDoubleStrike() {
        harness.addToBattlefield(player1, new KwendePrideOfFemeref());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's creature with first strike does not gain double strike")
    void opponentFirstStrikeCreatureNotAffected() {
        harness.addToBattlefield(player1, new KwendePrideOfFemeref());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());

        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double strike is lost when Kwende leaves the battlefield")
    void doubleStrikeLostWhenKwendeRemoved() {
        harness.addToBattlefield(player1, new KwendePrideOfFemeref());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());

        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isTrue();

        // Remove Kwende
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Kwende, Pride of Femeref"));

        // Knight should revert to just first strike
        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Kwende deals damage in both combat damage steps")
    void kwendeDealsDoubleStrikeCombatDamage() {
        addCreatureReady(player1, new KwendePrideOfFemeref());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("First-strike creatures deal damage in both combat damage steps")
    void grantedDoubleStrikeDealsCombatDamageTwice() {
        harness.addToBattlefield(player1, new KwendePrideOfFemeref());
        addCreatureReady(player1, new BenalishKnight());

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("First strike granted after Kwende enters grants double strike and loses it when removed")
    void laterGrantedFirstStrikeGainsAndLosesDoubleStrike() {
        harness.addToBattlefield(player1, new KwendePrideOfFemeref());
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new SteelLeafChampion());
        harness.setHand(player1, List.of(new Dub()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, champion.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, champion, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, champion, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Dub"));

        assertThat(gqs.hasKeyword(gd, champion, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, champion, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike granted before Kwende enters also grants double strike")
    void earlierGrantedFirstStrikeGainsDoubleStrike() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new SteelLeafChampion());
        harness.setHand(player1, List.of(new Dub()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, champion.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, champion, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.addToBattlefield(player1, new KwendePrideOfFemeref());

        assertThat(gqs.hasKeyword(gd, champion, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
