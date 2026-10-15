package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.Arachnoform;
import com.github.laxika.magicalvibes.cards.b.BroodWeaver;
import com.github.laxika.magicalvibes.cards.d.Drider;
import com.github.laxika.magicalvibes.cards.g.GlowstoneRecluse;
import com.github.laxika.magicalvibes.cards.g.GnottvoldRecluse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HatcherySpider;
import com.github.laxika.magicalvibes.cards.m.MammothSpider;
import com.github.laxika.magicalvibes.cards.n.NetcasterSpider;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.cards.s.SentinelSpider;
import com.github.laxika.magicalvibes.cards.s.Snarespinner;
import com.github.laxika.magicalvibes.cards.s.SpiderSpawning;
import com.github.laxika.magicalvibes.cards.s.SporecapSpider;
import com.github.laxika.magicalvibes.cards.s.SpideryGrasp;
import com.github.laxika.magicalvibes.cards.t.TwinSilkSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IshkanahBroodmother.class, TwinSilkSpider.class, Drider.class, BroodWeaver.class,
        GlowstoneRecluse.class, GnottvoldRecluse.class, HatcherySpider.class, MammothSpider.class,
        NetcasterSpider.class, SentinelSpider.class, Snarespinner.class, SporecapSpider.class,
        SpideryGrasp.class, SpiderSpawning.class, PreyUpon.class, Arachnoform.class,
        GrizzlyBears.class})
class IshkanahBroodmotherTest extends BaseCardTest {

    @Test
    void buffsOtherSpidersYouControl() {
        harness.addToBattlefield(player1, new IshkanahBroodmother());
        harness.addToBattlefield(player1, new TwinSilkSpider());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new TwinSilkSpider());

        Permanent spider = findPermanent(player1, "Twin-Silk Spider");
        Permanent ishkanah = findPermanent(player1, "Ishkanah, Broodmother");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentSpider = findPermanent(player2, "Twin-Silk Spider");

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ishkanah)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ishkanah)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSpider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSpider)).isEqualTo(2);
    }

    @Test
    void exilesTwoGraveyardCardsAndOffersASpellbookDraft() {
        harness.addToBattlefield(player1, new IshkanahBroodmother());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, gd.playerGraveyards.get(player1.getId()).stream()
                .limit(2)
                .map(Card::getId)
                .toList());
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
    }

    @Test
    void cannotActivateWithoutTwoCardsInYourGraveyard() {
        harness.addToBattlefield(player1, new IshkanahBroodmother());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blackManaPaysHybridCostAndNoncreaturesAreExiledBeforeResolution() {
        Permanent ishkanah = harness.addToBattlefieldAndReturn(player1, new IshkanahBroodmother());
        Card retained = new GnottvoldRecluse();
        Card firstCost = new Arachnoform();
        Card secondCost = new Arachnoform();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(retained, firstCost, secondCost));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(firstCost.getId(), secondCost.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(retained);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(firstCost, secondCost);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class)).isNull();
        assertThat(ishkanah.isTapped()).isFalse();

        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cards()).extracting(Card::getName).doesNotHaveDuplicates().hasSize(3)
                .isSubsetOf("Twin-Silk Spider", "Drider", "Brood Weaver", "Glowstone Recluse",
                        "Gnottvold Recluse", "Hatchery Spider", "Mammoth Spider", "Netcaster Spider",
                        "Sentinel Spider", "Snarespinner", "Sporecap Spider", "Spidery Grasp",
                        "Spider Spawning", "Prey Upon", "Arachnoform");
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drafted);
        assertThat(drafted.getOwnerId()).isEqualTo(player1.getId());
    }

    @Test
    void tappedSummoningSickIshkanahCanDraftRepeatedly() {
        Permanent ishkanah = harness.addToBattlefieldAndReturn(player1, new IshkanahBroodmother());
        ishkanah.tap();
        ishkanah.setSummoningSick(true);
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new Arachnoform(), new Arachnoform(),
                new GnottvoldRecluse(), new GnottvoldRecluse()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        for (int activation = 0; activation < 2; activation++) {
            harness.activateAbility(player1, 0, null, null);
            if (activation == 0) {
                harness.handleMultipleCardsChosen(player1, gd.playerGraveyards.get(player1.getId()).stream()
                        .limit(2).map(Card::getId).toList());
            }
            harness.passBothPriorities();
            PendingInteraction.SpellbookDraftChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
            assertThat(choice).isNotNull();
            harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(4);
        assertThat(ishkanah.isTapped()).isTrue();
    }

    @Test
    void creatureMadeASpiderByArachnoformReceivesTheAnthem() {
        harness.addToBattlefield(player1, new IshkanahBroodmother());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Arachnoform()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }
}
