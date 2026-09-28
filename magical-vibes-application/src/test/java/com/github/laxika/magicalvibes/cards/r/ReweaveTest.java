package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.j.JadeIdol;
import com.github.laxika.magicalvibes.cards.s.SenseisDiviningTop;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Reweave.class, ReachThroughMists.class, IsamaruHoundOfKonda.class,
        SenseisDiviningTop.class, Forest.class, HondenOfSeeingWinds.class, JadeIdol.class})
class ReweaveTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed creature is replaced by the first creature card revealed")
    void replacesCreatureWithCreature() {
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new Reweave()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.setLibrary(player1, List.of(new SenseisDiviningTop(), new IsamaruHoundOfKonda()));

        UUID targetId = harness.getPermanentId(player1, "Isamaru, Hound of Konda");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Isamaru, Hound of Konda");
        harness.assertOnBattlefield(player1, "Isamaru, Hound of Konda");
        // The artifact did not share a card type with the sacrificed creature, so it was shuffled back.
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sensei's Divining Top"));
    }

    @Test
    @DisplayName("Sacrificed artifact is replaced by an artifact, skipping a creature card")
    void replacesArtifactWithArtifact() {
        harness.addToBattlefield(player1, new SenseisDiviningTop());
        harness.setHand(player1, List.of(new Reweave()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.setLibrary(player1, List.of(new IsamaruHoundOfKonda(), new SenseisDiviningTop()));

        UUID targetId = harness.getPermanentId(player1, "Sensei's Divining Top");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sensei's Divining Top");
        harness.assertOnBattlefield(player1, "Sensei's Divining Top");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Isamaru, Hound of Konda"));
    }

    @Test
    @DisplayName("No card sharing a type — the whole library is revealed and shuffled back")
    void noSharedTypeInLibrary() {
        harness.addToBattlefield(player1, new SenseisDiviningTop());
        harness.setHand(player1, List.of(new Reweave()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.setLibrary(player1, List.of(new IsamaruHoundOfKonda(), new Forest()));

        UUID targetId = harness.getPermanentId(player1, "Sensei's Divining Top");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sensei's Divining Top");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can target an opponent's land — they sacrifice it and reveal until a land")
    void targetsOpponentLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Reweave()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.setLibrary(player2, List.of(new IsamaruHoundOfKonda(), new Forest()));

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Isamaru, Hound of Konda"));
    }

    @Test
    @DisplayName("Sacrificed enchantment is replaced by the first enchantment card revealed")
    void replacesEnchantmentWithEnchantment() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.setHand(player1, List.of(new Reweave()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.setLibrary(player1, List.of(new HondenOfSeeingWinds()));

        UUID targetId = harness.getPermanentId(player1, "Honden of Seeing Winds");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Honden of Seeing Winds");
        harness.assertOnBattlefield(player1, "Honden of Seeing Winds");
    }

    @Test
    @DisplayName("An empty library leaves only the sacrificed permanent in the graveyard")
    void emptyLibraryProducesNoReplacement() {
        harness.addToBattlefield(player1, new SenseisDiviningTop());
        harness.setHand(player1, List.of(new Reweave()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.setLibrary(player1, List.of());

        UUID targetId = harness.getPermanentId(player1, "Sensei's Divining Top");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sensei's Divining Top");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Uses the sacrificed permanent's current card types")
    void usesCurrentCardTypesOfSacrificedPermanent() {
        harness.addToBattlefield(player1, new JadeIdol());
        harness.setHand(player1, List.of(new Reweave()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.setLibrary(player1, List.of(new IsamaruHoundOfKonda(), new SenseisDiviningTop()));

        UUID targetId = harness.getPermanentId(player1, "Jade Idol");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jade Idol");
        harness.assertOnBattlefield(player1, "Isamaru, Hound of Konda");
        harness.assertNotOnBattlefield(player1, "Sensei's Divining Top");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sensei's Divining Top"));
    }

    @Test
    @DisplayName("Splices onto an Arcane spell, replaces a permanent, and stays in hand")
    void splicesOntoArcaneSpell() {
        harness.addToBattlefield(player2, new IsamaruHoundOfKonda());
        ReachThroughMists host = new ReachThroughMists();
        Reweave reweave = new Reweave();
        SenseisDiviningTop drawnCard = new SenseisDiviningTop();
        harness.setHand(player1, List.of(host, reweave));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLibrary(player2, List.of(new IsamaruHoundOfKonda()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Isamaru, Hound of Konda");
        harness.castWithSplice(player1, 0, targetId, List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Isamaru, Hound of Konda");
        harness.assertOnBattlefield(player2, "Isamaru, Hound of Konda");
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(reweave, drawnCard);
    }
}
