package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CrystalVein;
import com.github.laxika.magicalvibes.cards.p.PoliticalTrickery;
import com.github.laxika.magicalvibes.cards.u.UnyaroBeeSting;
import com.github.laxika.magicalvibes.cards.w.WaitingInTheWeeds;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Meddle.class, Boomerang.class, MtendaHerder.class, UnyaroBeeSting.class,
        WaitingInTheWeeds.class, PoliticalTrickery.class, CrystalVein.class})
class MeddleTest extends BaseCardTest {

    @Test
    @DisplayName("Meddle retargets a single-target creature spell to another creature")
    void retargetsCreatureTargetSpell() {
        UUID bears1PermId = harness.addToBattlefieldAndReturn(player1, new MtendaHerder()).getId();
        UUID bears2PermId = harness.addToBattlefieldAndReturn(player2, new MtendaHerder()).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Meddle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bears1PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bears2PermId);

        harness.handlePermanentChosen(player2, bears2PermId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears2PermId));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears1PermId));
    }

    @Test
    @DisplayName("Meddle does nothing when the target spell targets a player")
    void doesNothingWhenTargetIsNotACreature() {
        UnyaroBeeSting beeSting = new UnyaroBeeSting();
        harness.setHand(player1, List.of(beeSting));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Meddle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        GameData gd = harness.getGameData();
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, beeSting.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        StackEntry beeStingEntry = gd.stack.getLast();
        assertThat(beeStingEntry.getTargetId()).isEqualTo(player2.getId());

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore - 2);
    }

    @Test
    @DisplayName("Meddle does nothing when there is no other creature to redirect to")
    void doesNothingWithoutAnotherCreature() {
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, new MtendaHerder()).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Meddle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(bearsPermId);
    }

    @Test
    @DisplayName("Meddle does nothing when the target spell has no targets")
    void doesNothingWhenTargetSpellHasNoTargets() {
        WaitingInTheWeeds waitingInTheWeeds = new WaitingInTheWeeds();
        harness.castFromHand(player1, waitingInTheWeeds, "{1}{G}{G}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Meddle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, waitingInTheWeeds.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(waitingInTheWeeds.getId());
    }

    @Test
    @DisplayName("Meddle does nothing when the target spell has multiple targets")
    void doesNothingWhenTargetSpellHasMultipleTargets() {
        PoliticalTrickery politicalTrickery = new PoliticalTrickery();
        harness.setHand(player1, List.of(politicalTrickery));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID ownLandId = harness.addToBattlefieldAndReturn(player1, new CrystalVein()).getId();
        UUID opponentLandId = harness.addToBattlefieldAndReturn(player2, new CrystalVein()).getId();

        harness.castSorcery(player1, 0, List.of(ownLandId, opponentLandId));
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Meddle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, politicalTrickery.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(politicalTrickery.getId());
    }
}
