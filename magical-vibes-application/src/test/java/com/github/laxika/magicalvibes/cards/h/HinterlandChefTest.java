package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.b.Brushstrider;
import com.github.laxika.magicalvibes.cards.d.DeathbonnetSprout;
import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.cards.g.GildedGoose;
import com.github.laxika.magicalvibes.cards.h.HighlandGame;
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

@CardUsed({HinterlandChef.class, AlmightyBrushwagg.class, FrilledSandwalla.class,
        MossViper.class, Brushstrider.class, HighlandGame.class, IronshellBeetle.class,
        LotusCobra.class, KazanduNectarpot.class, GildedGoose.class, NessianHornbeetle.class,
        ScurridColony.class, TerritorialBoar.class, DeathbonnetSprout.class, SporeCrawler.class,
        MoldgrafMillipede.class})
class HinterlandChefTest extends BaseCardTest {

    @Test
    void entersDraftingAndBecomesAFoodArtifact() {
        Permanent chef = harness.enterBattlefieldAndReturn(player1, new HinterlandChef());
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gqs.isArtifact(gd, chef)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, chef, CardSubtype.FOOD)).isTrue();
    }

    @Test
    void gainsTheSacrificeAbilityAndCanBeUsedToGainLife() {
        Permanent chef = harness.enterBattlefieldAndReturn(player1, new HinterlandChef());
        resolveAllTriggers();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));

        chef.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(chef), null, null);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertInGraveyard(player1, "Hinterland Chef");
    }
}
