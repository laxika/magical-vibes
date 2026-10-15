package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.b.Brushstrider;
import com.github.laxika.magicalvibes.cards.d.DeathbonnetHulk;
import com.github.laxika.magicalvibes.cards.d.DeathbonnetSprout;
import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.cards.g.GildedGoose;
import com.github.laxika.magicalvibes.cards.i.IronshellBeetle;
import com.github.laxika.magicalvibes.cards.k.KazanduNectarpot;
import com.github.laxika.magicalvibes.cards.l.LotusCobra;
import com.github.laxika.magicalvibes.cards.m.MoldgrafMillipede;
import com.github.laxika.magicalvibes.cards.m.MossViper;
import com.github.laxika.magicalvibes.cards.n.NessianHornbeetle;
import com.github.laxika.magicalvibes.cards.s.ScurridColony;
import com.github.laxika.magicalvibes.cards.s.SporeCrawler;
import com.github.laxika.magicalvibes.cards.t.TerritorialBoar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HinterlandChef.class, AlmightyBrushwagg.class, FrilledSandwalla.class,
        MossViper.class, Brushstrider.class, HighlandGame.class, IronshellBeetle.class,
        LotusCobra.class, KazanduNectarpot.class, GildedGoose.class, NessianHornbeetle.class,
        ScurridColony.class, TerritorialBoar.class, DeathbonnetSprout.class, DeathbonnetHulk.class,
        SporeCrawler.class,
        MoldgrafMillipede.class})
class HinterlandChefTest extends BaseCardTest {

    @Test
    void entersDraftingAndTheDraftedCardBecomesAFoodArtifact() {
        harness.setHand(player2, List.of());
        Permanent chef = harness.enterBattlefieldAndReturn(player1, new HinterlandChef());
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards()).extracting(Card::getName).doesNotHaveDuplicates();

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        harness.assertInHand(player1, drafted.getName());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        Permanent food = castDraftedCard(drafted, chef);
        assertThat(gqs.isArtifact(gd, food)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, food, CardSubtype.FOOD)).isTrue();
        assertThat(gqs.isCreature(gd, food)).isTrue();
        assertThat(gqs.isArtifact(gd, chef)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, chef, CardSubtype.FOOD)).isFalse();
    }

    @Test
    void draftedCardGainsTheSacrificeAbilityAndCanBeUsedToGainLife() {
        Permanent chef = harness.enterBattlefieldAndReturn(player1, new HinterlandChef());
        Card drafted = completeDraft();
        Permanent food = castDraftedCard(drafted, chef);

        food.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activateFoodAbility(food);
        harness.assertInGraveyard(player1, drafted.getName());
        harness.assertOnBattlefield(player1, "Hinterland Chef");
        resolveAllTriggers();

        harness.assertLife(player1, drafted instanceof HighlandGame ? 25 : 23);
    }

    @Test
    void draftedFoodCreatureCannotTapWhileSummoningSick() {
        Permanent chef = harness.enterBattlefieldAndReturn(player1, new HinterlandChef());
        Card drafted = completeDraft();
        Permanent food = castDraftedCard(drafted, chef);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> activateFoodAbility(food))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, drafted.getName());
        harness.assertNotInGraveyard(player1, drafted.getName());
        harness.assertLife(player1, 20);
    }

    @Test
    void draftedCardKeepsFoodCharacteristicsAndAbilityAfterReturningToHand() {
        Permanent chef = harness.enterBattlefieldAndReturn(player1, new HinterlandChef());
        Card drafted = completeDraft();
        Permanent food = castDraftedCard(drafted, chef);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, food));
        harness.assertInHand(player1, drafted.getName());

        Card returned = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals(drafted.getName())).findFirst().orElseThrow();
        Permanent recast = castDraftedCard(returned, chef);
        assertThat(gqs.isArtifact(gd, recast)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, recast, CardSubtype.FOOD)).isTrue();
        recast.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activateFoodAbility(recast);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, drafted.getName());
        harness.assertLife(player1, drafted instanceof HighlandGame ? 25 : 23);
    }

    private Card completeDraft() {
        resolveAllTriggers();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        return drafted;
    }

    private Permanent castDraftedCard(Card drafted, Permanent chef) {
        harness.setLibrary(player1, List.of(new HinterlandChef()));
        if (drafted instanceof IronshellBeetle) {
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(drafted), chef.getId());
        } else {
            String cost = switch (drafted.getName()) {
                case "Almighty Brushwagg", "Frilled Sandwalla", "Moss Viper", "Gilded Goose",
                        "Deathbonnet Sprout" -> "{G}";
                case "Spore Crawler" -> "{2}{G}";
                case "Moldgraf Millipede" -> "{4}{G}";
                default -> "{1}{G}";
            };
            harness.castFromHand(player1, drafted, cost);
        }
        resolveAllTriggers();
        return findPermanent(player1, drafted.getName());
    }

    private void activateFoodAbility(Permanent food) {
        int abilityIndex = switch (food.getCard().getName()) {
            case "Almighty Brushwagg", "Frilled Sandwalla" -> 1;
            case "Gilded Goose" -> 2;
            default -> 0;
        };
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food),
                abilityIndex, null, null);
    }
}
