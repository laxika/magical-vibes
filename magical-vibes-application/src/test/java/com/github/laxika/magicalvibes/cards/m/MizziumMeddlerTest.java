package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.SendToSleep;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MizziumMeddler.class, Boomerang.class, GrizzlyBears.class, LavaAxe.class,
        LlanowarElves.class, ProdigalSorcerer.class, SendToSleep.class})
class MizziumMeddlerTest extends BaseCardTest {

    @Test
    @DisplayName("Flashed in, it redirects a targeted spell to itself")
    void redirectsTargetedSpellToItself() {
        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, bears).getId();

        harness.forceActivePlayer(player2);
        Boomerang boomerang = new Boomerang();
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new MizziumMeddler()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        // Resolve the Meddler → it enters and its ETB trigger asks for a stack target.
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(boomerang.getId());
        harness.handlePermanentChosen(player1, boomerang.getId());

        // Resolve the ETB trigger (redirect), then Boomerang itself.
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mizzium Meddler");
    }

    @Test
    @DisplayName("An activated ability on the stack is a legal target too")
    void redirectsActivatedAbility() {
        ProdigalSorcerer sorcerer = new ProdigalSorcerer();
        Permanent sorcererPerm = harness.addToBattlefieldAndReturn(player2, sorcerer);
        sorcererPerm.setSummoningSick(false);
        UUID elvesPermId = harness.addToBattlefieldAndReturn(player1, new LlanowarElves()).getId();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, elvesPermId);
        UUID abilityId = harness.getGameData().stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new MizziumMeddler()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(abilityId);
        harness.handlePermanentChosen(player1, abilityId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        // The 1 damage hit the 1/4 Meddler instead of the 1/1 Elves.
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Mizzium Meddler");
    }

    @Test
    @DisplayName("Nothing changes when the Meddler is not a legal target for the spell")
    void doesNothingWhenNotALegalTarget() {
        harness.forceActivePlayer(player2);
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player2, List.of(lavaAxe));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new MizziumMeddler()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, lavaAxe.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        // Lava Axe targets a player or planeswalker; the Meddler is not a legal new target.
        harness.assertLife(player1, 15);
    }

    @Test
    @CardUsed({MizziumMeddler.class, SendToSleep.class})
    @DisplayName("May decline to redirect a legal creature target")
    void mayDeclineRedirection() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new MizziumMeddler());
        harness.forceActivePlayer(player2);
        SendToSleep spell = new SendToSleep();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, List.of(original.getId()));
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new MizziumMeddler()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(
                PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(original.isTapped()).isTrue();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> !permanent.getId().equals(original.getId()))
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isFalse());
    }

    @Test
    @CardUsed({MizziumMeddler.class, SendToSleep.class})
    @DisplayName("Changes exactly one of a spell's two targets")
    void redirectsOneOfTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MizziumMeddler());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MizziumMeddler());
        harness.forceActivePlayer(player2);
        SendToSleep spell = new SendToSleep();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, List.of(first.getId(), second.getId()));
        harness.passPriority(player2);

        MizziumMeddler entering = new MizziumMeddler();
        harness.setHand(player1, List.of(entering));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent meddler = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(entering.getId()))
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();
        if (harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        if (harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, first.getId());
        }
        harness.passBothPriorities();

        assertThat(meddler.isTapped()).isTrue();
        assertThat(List.of(first, second)).filteredOn(Permanent::isTapped).hasSize(1);
    }

    @Test
    @CardUsed({MizziumMeddler.class, SendToSleep.class})
    @DisplayName("Can target a spell with no targets without changing anything")
    void canTargetSpellWithNoTargets() {
        SendToSleep spell = new SendToSleep();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, List.<UUID>of());
        harness.passPriority(player2);
        harness.setHand(player1, List.of(new MizziumMeddler()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class).validIds()).contains(spell.getId());
        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mizzium Meddler");
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isFalse());
        harness.assertInGraveyard(player2, "Send to Sleep");
    }

    @Test
    @CardUsed({MizziumMeddler.class, SendToSleep.class})
    @DisplayName("Redirects a spell using a target list with one creature")
    void redirectsSingleTargetInTargetList() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new MizziumMeddler());
        harness.forceActivePlayer(player2);
        SendToSleep spell = new SendToSleep();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, List.of(original.getId()));
        harness.passPriority(player2);

        MizziumMeddler entering = new MizziumMeddler();
        harness.setHand(player1, List.of(entering));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent meddler = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(entering.getId()))
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();
        if (harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.passBothPriorities();

        assertThat(original.isTapped()).isFalse();
        assertThat(meddler.isTapped()).isTrue();
    }
}
