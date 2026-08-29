package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenSkirmisher;
import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.cards.z.ZombieOutlander;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoulflayerTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles cards and grants Soulflayer keywords from exiled creature cards")
    void gainsKeywordsFromCreatureCardsExiledWithDelve() {
        List<Card> graveyard = new ArrayList<>(List.of(
                new AvenSkirmisher(), new VampireNighthawk(), new DarksteelMyr()));
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new Soulflayer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        Permanent soulflayer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Soulflayer)
                .findFirst()
                .orElseThrow();

        assertThat(gqs.hasKeyword(gd, soulflayer, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, soulflayer, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, soulflayer, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, soulflayer, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.getCardsExiledByPermanent(soulflayer.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
    }

    @Test
    @DisplayName("Does not gain abilities from a noncreature card exiled with delve")
    void ignoresNoncreatureCardsExiledWithDelve() {
        Card noncreature = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(noncreature)));
        harness.setHand(player1, List.of(new Soulflayer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();

        Permanent soulflayer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Soulflayer)
                .findFirst()
                .orElseThrow();

        assertThat(gqs.computeStaticBonus(gd, soulflayer).keywords())
                .doesNotContain(Keyword.FLYING, Keyword.DEATHTOUCH, Keyword.LIFELINK, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Does not gain unlisted abilities from creature cards exiled with delve")
    void gainsOnlyListedAbilities() {
        List<Card> graveyard = new ArrayList<>(List.of(new BogWraith(), new ZombieOutlander()));
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new Soulflayer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));
        harness.passBothPriorities();

        Permanent soulflayer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Soulflayer)
                .findFirst()
                .orElseThrow();

        assertThat(gqs.computeStaticBonus(gd, soulflayer).keywords()).doesNotContain(Keyword.SWAMPWALK);
        assertThat(gqs.hasProtectionFrom(gd, soulflayer, CardColor.GREEN)).isFalse();
    }
}
