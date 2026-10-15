package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientImperiosaur;
import com.github.laxika.magicalvibes.cards.b.BurningSunsAvatar;
import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.c.ChargingMonstrosaur;
import com.github.laxika.magicalvibes.cards.e.EtaliPrimalConqueror;
import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GhaltaPrimalHunger;
import com.github.laxika.magicalvibes.cards.g.GishathSunsAvatar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QuartzwoodCrasher;
import com.github.laxika.magicalvibes.cards.r.RegisaurAlpha;
import com.github.laxika.magicalvibes.cards.r.RipjawRaptor;
import com.github.laxika.magicalvibes.cards.t.TerritorialAllosaurus;
import com.github.laxika.magicalvibes.cards.t.TranquilFrillback;
import com.github.laxika.magicalvibes.cards.v.VerdantSunsAvatar;
import com.github.laxika.magicalvibes.cards.z.ZacamaPrimalCalamity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScalespeakerShepherd.class, AncientImperiosaur.class, BurningSunsAvatar.class,
        CarnageTyrant.class, ChargingMonstrosaur.class, EtaliPrimalConqueror.class,
        GhaltaPrimalHunger.class, GishathSunsAvatar.class, QuartzwoodCrasher.class,
        RegisaurAlpha.class, RipjawRaptor.class, ShiftingCeratops.class,
        TerritorialAllosaurus.class, TranquilFrillback.class, VerdantSunsAvatar.class,
        ZacamaPrimalCalamity.class, FrenziedRaptor.class, GrizzlyBears.class})
class ScalespeakerShepherdTest extends BaseCardTest {

    @Test
    void entersAndDraftsASpellbookCard() {
        harness.enterBattlefieldAndReturn(player1, new ScalespeakerShepherd());
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards()).extracting(Card::getName).doesNotHaveDuplicates()
                .allMatch(List.of("Ancient Imperiosaur", "Burning Sun's Avatar", "Carnage Tyrant",
                        "Charging Monstrosaur", "Etali, Primal Conqueror", "Ghalta, Primal Hunger",
                        "Gishath, Sun's Avatar", "Quartzwood Crasher", "Regisaur Alpha",
                        "Ripjaw Raptor", "Shifting Ceratops", "Territorial Allosaurus",
                        "Tranquil Frillback", "Verdant Sun's Avatar", "Zacama, Primal Calamity")::contains);

        Card drafted = choice.cards().getFirst();
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1).contains(drafted);
    }

    @Test
    void dinosaurSpellsCostOneLessToCast() {
        harness.addToBattlefield(player1, new ScalespeakerShepherd());
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void nonDinosaurSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new ScalespeakerShepherd());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleShepherdsStackTheirReductions() {
        harness.addToBattlefield(player1, new ScalespeakerShepherd());
        harness.addToBattlefield(player1, new ScalespeakerShepherd());
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reductionsCannotPayTheColoredManaRequirement() {
        harness.addToBattlefield(player1, new ScalespeakerShepherd());
        harness.addToBattlefield(player1, new ScalespeakerShepherd());
        harness.addToBattlefield(player1, new ScalespeakerShepherd());
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsShepherdDoesNotReduceYourDinosaurSpells() {
        harness.addToBattlefield(player2, new ScalespeakerShepherd());
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
