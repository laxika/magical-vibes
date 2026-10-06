package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SilverBolt;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiteOfOblivion.class, Forest.class, GrizzlyBears.class, Spellbook.class, DawnhartRejuvenator.class, SilverBolt.class})
class RiteOfOblivionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a nonland permanent and exiles the target")
    void sacrificesNonlandPermanentAndExilesTarget() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());
        giveRiteFromHand();

        harness.castSorceryWithSacrifice(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player1, "Spellbook"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new Forest());
        giveRiteFromHand();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0,
                harness.getPermanentId(player2, "Forest"),
                harness.getPermanentId(player1, "Spellbook")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    @DisplayName("Cannot sacrifice a land as an additional cost")
    void cannotSacrificeLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        giveRiteFromHand();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player1, "Forest")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    @DisplayName("Flashback sacrifices a nonland permanent, exiles the target, and exiles the spell")
    void flashbackSacrificesAndExilesSpell() {
        RiteOfOblivion spell = new RiteOfOblivion();
        harness.setGraveyard(player1, List.of(spell));
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFlashbackWithSacrifice(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player1, "Spellbook"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void canSacrificeCreatureToExileOwnArtifact() {
        harness.addToBattlefield(player1, new DawnhartRejuvenator());
        harness.addToBattlefield(player1, new SilverBolt());
        giveRiteFromHand();

        harness.castSorceryWithSacrifice(player1, 0,
                harness.getPermanentId(player1, "Silver Bolt"),
                harness.getPermanentId(player1, "Dawnhart Rejuvenator"));

        harness.assertInGraveyard(player1, "Dawnhart Rejuvenator");
        harness.assertOnBattlefield(player1, "Silver Bolt");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Silver Bolt");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Silver Bolt"));
        harness.assertInGraveyard(player1, "Rite of Oblivion");
    }

    @Test
    void canTargetThePermanentSacrificedForItsCost() {
        harness.addToBattlefield(player1, new SilverBolt());
        giveRiteFromHand();
        var permanentId = harness.getPermanentId(player1, "Silver Bolt");

        harness.castSorceryWithSacrifice(player1, 0, permanentId, permanentId);
        harness.assertInGraveyard(player1, "Silver Bolt");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rite of Oblivion");
        harness.assertInGraveyard(player1, "Silver Bolt");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashbackExilesSpellWhenItsTargetWasSacrificedForTheCost() {
        RiteOfOblivion spell = new RiteOfOblivion();
        harness.setGraveyard(player1, List.of(spell));
        harness.addToBattlefield(player1, new SilverBolt());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        var permanentId = harness.getPermanentId(player1, "Silver Bolt");

        harness.castFlashbackWithSacrifice(player1, 0, permanentId, permanentId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Silver Bolt");
        harness.assertNotInGraveyard(player1, "Rite of Oblivion");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeOpponentsPermanent() {
        harness.addToBattlefield(player1, new SilverBolt());
        harness.addToBattlefield(player2, new DawnhartRejuvenator());
        giveRiteFromHand();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0,
                harness.getPermanentId(player1, "Silver Bolt"),
                harness.getPermanentId(player2, "Dawnhart Rejuvenator")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");

        harness.assertOnBattlefield(player1, "Silver Bolt");
        harness.assertOnBattlefield(player2, "Dawnhart Rejuvenator");
        harness.assertInHand(player1, "Rite of Oblivion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashbackCannotOmitAdditionalSacrificeCost() {
        harness.setGraveyard(player1, List.of(new RiteOfOblivion()));
        harness.addToBattlefield(player1, new SilverBolt());
        harness.addToBattlefield(player2, new DawnhartRejuvenator());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0,
                harness.getPermanentId(player2, "Dawnhart Rejuvenator")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");

        harness.assertInGraveyard(player1, "Rite of Oblivion");
        harness.assertOnBattlefield(player1, "Silver Bolt");
        harness.assertOnBattlefield(player2, "Dawnhart Rejuvenator");
        assertThat(gd.stack).isEmpty();
    }

    private void giveRiteFromHand() {
        harness.setHand(player1, List.of(new RiteOfOblivion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
