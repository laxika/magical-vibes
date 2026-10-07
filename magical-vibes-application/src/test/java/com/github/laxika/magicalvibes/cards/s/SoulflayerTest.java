package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenSkirmisher;
import com.github.laxika.magicalvibes.cards.a.AleshaWhoSmilesAtDeath;
import com.github.laxika.magicalvibes.cards.a.ArchersOfQarsi;
import com.github.laxika.magicalvibes.cards.a.AvenSunstriker;
import com.github.laxika.magicalvibes.cards.b.BattleBrawler;
import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.k.KnightOfGrace;
import com.github.laxika.magicalvibes.cards.l.LightningShrieker;
import com.github.laxika.magicalvibes.cards.o.OjutaiSoulOfWinter;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.cards.z.ZombieOutlander;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Soulflayer.class, AvenSkirmisher.class, BogWraith.class, DarksteelMyr.class,
        Shock.class, VampireNighthawk.class, ZombieOutlander.class, AleshaWhoSmilesAtDeath.class,
        ArchersOfQarsi.class, AvenSunstriker.class, BattleBrawler.class, KnightOfGrace.class,
        LightningShrieker.class, OjutaiSoulOfWinter.class, PullFromEternity.class,
        SilumgarTheDriftingDeath.class})
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

        Permanent soulflayer = findPermanent(player1, "Soulflayer");

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

        Permanent soulflayer = findPermanent(player1, "Soulflayer");

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

        Permanent soulflayer = findPermanent(player1, "Soulflayer");

        assertThat(gqs.computeStaticBonus(gd, soulflayer).keywords()).doesNotContain(Keyword.SWAMPWALK);
        assertThat(gqs.hasProtectionFrom(gd, soulflayer, CardColor.GREEN)).isFalse();
    }

    @Test
    void gainsFirstStrikeHasteReachTrampleAndVigilance() {
        harness.setGraveyard(player1, List.of(new AleshaWhoSmilesAtDeath(),
                new LightningShrieker(), new ArchersOfQarsi(), new OjutaiSoulOfWinter()));
        harness.setHand(player1, List.of(new Soulflayer()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3));
        harness.passBothPriorities();

        Permanent soulflayer = findPermanent(player1, "Soulflayer");
        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.HASTE,
                Keyword.REACH, Keyword.TRAMPLE, Keyword.VIGILANCE)) {
            assertThat(gqs.hasKeyword(gd, soulflayer, keyword)).as("%s", keyword).isTrue();
        }
        assertThat(gqs.hasKeyword(gd, soulflayer, Keyword.DEFENDER)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void gainsDoubleStrikeAndHexproof() {
        harness.setGraveyard(player1, List.of(new AvenSunstriker(), new SilumgarTheDriftingDeath()));
        harness.setHand(player1, List.of(new Soulflayer()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));
        harness.passBothPriorities();

        Permanent soulflayer = findPermanent(player1, "Soulflayer");
        assertThat(gqs.hasKeyword(gd, soulflayer, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, soulflayer, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void doesNotGainConditionallyGrantedFirstStrike() {
        harness.addToBattlefield(player1, new AvenSkirmisher());
        harness.setGraveyard(player1, List.of(new BattleBrawler()));
        harness.setHand(player1, List.of(new Soulflayer()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Soulflayer"), Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void ignoresCreatureCardsNotSelectedForDelve() {
        Card unselected = new AvenSkirmisher();
        harness.setGraveyard(player1, List.of(new BattleBrawler(), unselected));
        harness.setHand(player1, List.of(new Soulflayer()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Soulflayer"), Keyword.FLYING)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
    }

    @Test
    void canCastWithoutDelvingAndGainsNoKeywords() {
        harness.setGraveyard(player1, List.of(new LightningShrieker()));
        harness.setHand(player1, List.of(new Soulflayer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent soulflayer = findPermanent(player1, "Soulflayer");
        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.HASTE, Keyword.TRAMPLE)) {
            assertThat(gqs.hasKeyword(gd, soulflayer, keyword)).as("%s", keyword).isFalse();
        }
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotDelveMoreThanGenericManaRequirement() {
        harness.setGraveyard(player1, List.of(new AvenSkirmisher(), new AvenSkirmisher(),
                new AvenSkirmisher(), new AvenSkirmisher(), new AvenSkirmisher()));
        harness.setHand(player1, List.of(new Soulflayer()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4))).isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotUseDelveToPayColoredMana() {
        harness.setGraveyard(player1, List.of(new AvenSkirmisher(), new AvenSkirmisher(),
                new AvenSkirmisher(), new AvenSkirmisher()));
        harness.setHand(player1, List.of(new Soulflayer()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3))).isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void retainsFlyingAfterDelvedCardLeavesExile() {
        Card delvedCard = new AvenSkirmisher();
        harness.setGraveyard(player1, List.of(delvedCard));
        harness.setHand(player1, List.of(new Soulflayer(), new PullFromEternity()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();
        Permanent soulflayer = findPermanent(player1, "Soulflayer");
        assertThat(gqs.hasKeyword(gd, soulflayer, Keyword.FLYING)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, delvedCard.getId());

        assertThat(gd.findExiledCard(delvedCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(delvedCard);
        assertThat(gqs.hasKeyword(gd, soulflayer, Keyword.FLYING)).isTrue();
    }

    @Test
    void gainsFlyingEvenIfDelvedCardLeavesExileBeforeSoulflayerResolves() {
        Card delvedCard = new AvenSkirmisher();
        harness.setGraveyard(player1, List.of(delvedCard));
        harness.setHand(player1, List.of(new Soulflayer()));
        harness.setHand(player2, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.castAndResolveInstant(player2, 0, delvedCard.getId());
        assertThat(gd.findExiledCard(delvedCard.getId())).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(delvedCard);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Soulflayer"), Keyword.FLYING)).isTrue();
    }

    @Test
    void gainsSpecificHexproofVariantFromDelvedCreature() {
        harness.setGraveyard(player1, List.of(new KnightOfGrace()));
        harness.setHand(player1, List.of(new Soulflayer()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();

        Permanent soulflayer = findPermanent(player1, "Soulflayer");
        assertThat(gqs.hasKeyword(gd, soulflayer, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasHexproofFromColor(gd, soulflayer, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasHexproofFromColor(gd, soulflayer, CardColor.RED)).isFalse();
        assertThat(gqs.hasKeyword(gd, soulflayer, Keyword.HEXPROOF)).isFalse();
    }
}
