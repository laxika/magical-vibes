package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrawlingChorus.class, WrathOfGod.class})
class CrawlingChorusTest extends BaseCardTest {

    private Permanent createMite() {
        harness.addToBattlefield(player1, new CrawlingChorus());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        return findPermanent(player1, "Mite");
    }

    @Test
    @DisplayName("When Crawling Chorus dies, it creates a Phyrexian Mite")
    void deathCreatesMite() {
        Permanent mite = createMite();

        assertThat(mite.getCard().isToken()).isTrue();
        assertThat(mite.getCard().getPower()).isEqualTo(1);
        assertThat(mite.getCard().getToughness()).isEqualTo(1);
        assertThat(mite.getCard().getColor()).isNull();
        assertThat(mite.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(mite.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(mite.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.MITE);
        assertThat(mite.getCard().getKeywords()).contains(Keyword.TOXIC);
        assertThat(bls.canBlock(gd, mite)).isFalse();
    }

    @Test
    @DisplayName("The created Mite gives a poison counter when it deals combat damage")
    void miteGivesPoisonOnCombatDamage() {
        Permanent mite = createMite();
        mite.setSummoningSick(false);
        mite.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Crawling Chorus gives poison with combat damage without using the stack")
    void chorusToxicAppliesWithDamage() {
        Permanent chorus = addCreatureReady(player1, new CrawlingChorus());
        chorus.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Mite gives poison with combat damage without using the stack")
    void miteToxicAppliesWithDamage() {
        Permanent mite = createMite();
        mite.setSummoningSick(false);
        mite.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each dying Chorus creates its Mite for its controller after the death trigger resolves")
    void simultaneousDeathsCreateMitesForEachController() {
        harness.addToBattlefield(player1, new CrawlingChorus());
        harness.addToBattlefield(player2, new CrawlingChorus());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Crawling Chorus");
        harness.assertInGraveyard(player2, "Crawling Chorus");
        assertThat(countPermanents(player1, "Mite")).isZero();
        assertThat(countPermanents(player2, "Mite")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mite")).isEqualTo(1);
        assertThat(countPermanents(player2, "Mite")).isEqualTo(1);
    }
}
