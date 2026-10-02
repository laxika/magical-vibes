package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArcaneEncyclopedia;
import com.github.laxika.magicalvibes.cards.d.DaredevilDragster;
import com.github.laxika.magicalvibes.cards.d.DiamondMare;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.FiligreeFamiliar;
import com.github.laxika.magicalvibes.cards.f.FountainOfRenewal;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuildGlobe;
import com.github.laxika.magicalvibes.cards.h.HeraldicBanner;
import com.github.laxika.magicalvibes.cards.h.HonoredHeirloom;
import com.github.laxika.magicalvibes.cards.k.KeyToTheCity;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.StuffedBear;
import com.github.laxika.magicalvibes.cards.t.TreasureVault;
import com.github.laxika.magicalvibes.cards.z.ZephyrBoots;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BigSpender.class, GrizzlyBears.class, DarksteelRelic.class, MindStone.class,
        StuffedBear.class, DaredevilDragster.class, HonoredHeirloom.class, TreasureVault.class,
        GildedLotus.class, HeraldicBanner.class, KeyToTheCity.class, PropheticPrism.class,
        FiligreeFamiliar.class, GoldenEgg.class, FountainOfRenewal.class, GuildGlobe.class,
        ZephyrBoots.class, ArcaneEncyclopedia.class, DiamondMare.class})
class BigSpenderTest extends BaseCardTest {

    private static final List<String> SPELLBOOK = List.of(
            "Stuffed Bear", "Daredevil Dragster", "Honored Heirloom", "Treasure Vault",
            "Gilded Lotus", "Heraldic Banner", "Key to the City", "Prophetic Prism",
            "Filigree Familiar", "Golden Egg", "Fountain of Renewal", "Guild Globe",
            "Zephyr Boots", "Arcane Encyclopedia", "Diamond Mare");

    @Test
    @DisplayName("Creates one Treasure when one or more creatures become blocked")
    void createsOneTreasureForSeveralBlockedCreatures() {
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        secondAttacker.setAttacking(true);
        addCreatureReady(player1, new BigSpender());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    @DisplayName("Sacrifices two artifacts to draft from its spellbook")
    void sacrificesTwoArtifactsAndDrafts() {
        harness.addToBattlefield(player1, new BigSpender());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent spareRelic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice cost =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(cost).isNotNull();
        assertThat(cost.validIds()).containsExactlyInAnyOrder(relic.getId(), mindStone.getId(), spareRelic.getId());
        harness.handlePermanentChosen(player1, relic.getId());
        harness.handlePermanentChosen(player1, mindStone.getId());

        harness.assertInGraveyard(player1, "Darksteel Relic");
        harness.assertInGraveyard(player1, "Mind Stone");
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
