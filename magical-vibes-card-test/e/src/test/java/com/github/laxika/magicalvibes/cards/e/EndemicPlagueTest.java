package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GravebaneZombie;
import com.github.laxika.magicalvibes.cards.g.GoblinSkyRaider;
import com.github.laxika.magicalvibes.cards.h.Hystrodon;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EndemicPlague.class, ElvishWarrior.class, WirewoodElf.class, GoblinSkyRaider.class,
        GlorySeeker.class, Hystrodon.class})
class EndemicPlagueTest extends BaseCardTest {

    @Test
    void destroysAllCreaturesSharingAnySacrificedCreatureType() {
        Permanent sacrificed = addCreatureReady(player1, new ElvishWarrior());
        harness.addToBattlefield(player1, new WirewoodElf());
        harness.addToBattlefield(player2, new GoblinSkyRaider());
        harness.addToBattlefield(player2, new GlorySeeker());
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Warrior");
        harness.assertNotOnBattlefield(player1, "Wirewood Elf");
        harness.assertNotOnBattlefield(player2, "Goblin Sky Raider");
        harness.assertOnBattlefield(player2, "Glory Seeker");
    }

    @Test
    void destructionDoesNotAllowRegeneration() {
        Permanent sacrificed = addCreatureReady(player1, new ElvishWarrior());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new WirewoodElf());
        matchingCreature.setRegenerationShield(1);
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wirewood Elf");
        harness.assertInGraveyard(player2, "Wirewood Elf");
    }

    @Test
    void cannotCastWithoutACreatureToSacrifice() {
        harness.setHand(player1, List.of(new EndemicPlague()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void sacrificingAFaceDownCreatureDoesNotDestroyCreaturesOfItsPrintedType() {
        harness.setHand(player1, List.of(new Hystrodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent sacrificed = findPermanent(player1, "Hystrodon");
        harness.addToBattlefield(player2, new Hystrodon());
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hystrodon");
        harness.assertOnBattlefield(player2, "Hystrodon");
    }

    @Test
    void sacrificeIsPaidBeforeResolutionAndNewMatchingCreaturesAreDestroyed() {
        Permanent sacrificed = addCreatureReady(player1, new ElvishWarrior());
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrificed.getId());

        harness.assertInGraveyard(player1, "Elvish Warrior");
        harness.assertNotOnBattlefield(player1, "Elvish Warrior");
        harness.addToBattlefield(player2, new WirewoodElf());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wirewood Elf");
    }

    @Test
    void cannotSacrificeAnOpponentsCreature() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Elvish Warrior");
    }

    @Test
    @CardUsed({ScatheZombies.class, GravebaneZombie.class, KalitasTraitorOfGhet.class})
    @DisplayName("Gravebane Zombie's death replacement is chosen before any creature is destroyed")
    void asksForTheDeathReplacementBeforeAnyCreatureIsDestroyed() {
        Permanent sacrificed = addCreatureReady(player1, new ScatheZombies());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent gravebane = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.addToBattlefield(player2, new ScatheZombies());
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.stack).extracting(entry -> entry.getCard().getName()).containsExactly("Endemic Plague");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        harness.assertOnBattlefield(player2, "Scathe Zombies");

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        assertThat(gd.findExiledCard(gravebane.getCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        assertThat(zombieTokenCount()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new EndemicPlague()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private long zombieTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getSubtypes().contains(CardSubtype.ZOMBIE))
                .count();
    }
}
