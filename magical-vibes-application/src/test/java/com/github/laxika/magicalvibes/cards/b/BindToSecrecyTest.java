package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CorpseChurn;
import com.github.laxika.magicalvibes.cards.c.CorpseHauler;
import com.github.laxika.magicalvibes.cards.c.CourierBat;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DurableCoilbug;
import com.github.laxika.magicalvibes.cards.f.FearOfDeath;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GorgingVulture;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LilianasElite;
import com.github.laxika.magicalvibes.cards.l.LockedInTheCemetery;
import com.github.laxika.magicalvibes.cards.n.NagaOracle;
import com.github.laxika.magicalvibes.cards.n.NecroticWound;
import com.github.laxika.magicalvibes.cards.o.ObsessiveStitcher;
import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.StrategicPlanning;
import com.github.laxika.magicalvibes.cards.u.UnmarkedGrave;
import com.github.laxika.magicalvibes.cards.w.Wonder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BindToSecrecy.class, CorpseChurn.class, CorpseHauler.class, CourierBat.class,
        DurableCoilbug.class, FearOfDeath.class, GorgingVulture.class, LilianasElite.class,
        LockedInTheCemetery.class, NagaOracle.class, NecroticWound.class, ObsessiveStitcher.class,
        ReassemblingSkeleton.class, StrategicPlanning.class, UnmarkedGrave.class, Wonder.class,
        Divination.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class, Spellbook.class})
class BindToSecrecyTest extends BaseCardTest {

    private static final List<String> SPELLBOOK = List.of(
            "Corpse Churn", "Corpse Hauler", "Courier Bat", "Durable Coilbug", "Fear of Death",
            "Gorging Vulture", "Liliana's Elite", "Locked in the Cemetery", "Naga Oracle",
            "Necrotic Wound", "Obsessive Stitcher", "Reassembling Skeleton", "Strategic Planning",
            "Unmarked Grave", "Wonder");

    @Test
    void countersNoncreatureSpell() {
        Spellbook spellbook = new Spellbook();
        harness.setHand(player2, List.of(spellbook));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.setHand(player1, List.of(new BindToSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passPriority(player2);
        harness.castModalInstantWithModes(player1, 0, 1, 1, new int[]{0}, spellbook.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Spellbook");
        harness.assertInGraveyard(player1, "Bind to Secrecy");
    }

    @Test
    void counterModeRejectsCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.setHand(player1, List.of(new BindToSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 1, new int[]{0}, bears.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void conjuresOpponentGraveyardCreatureWithPerpetualAnyColorPermission() {
        GrizzlyBears opponentCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new BindToSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 1, new int[]{1}, null,
                List.of(opponentCard.getId()));
        harness.passBothPriorities();

        Card duplicate = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears") && card.isTokenCard())
                .findFirst().orElseThrow();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);

        int duplicateIndex = gd.playerHands.get(player1.getId()).indexOf(duplicate);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, duplicateIndex);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void draftsFromSpellbookWithFiveDistinctGraveyardManaValues() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new Divination(), new HillGiant()));
        Spellbook spellbook = new Spellbook();
        harness.setHand(player2, List.of(spellbook));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.setHand(player1, List.of(new BindToSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passPriority(player2);
        harness.castModalInstantWithModes(player1, 0, 1, 1, new int[]{0}, spellbook.getId(), List.of());
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards()).extracting(Card::getName).allMatch(SPELLBOOK::contains);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
    }
}
