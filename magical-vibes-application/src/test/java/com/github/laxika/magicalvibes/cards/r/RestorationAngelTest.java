package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RestorationAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ETB flickers a non-Angel creature you control immediately")
    void flickersOwnNonAngelCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RestorationAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> ETB MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Returned creature has summoning sickness")
    void returnedCreatureHasSummoningSickness() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RestorationAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Declining may ability does not exile anything")
    void decliningMaySkipsExile() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RestorationAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Restoration Angel"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Angel you control is not a legal target — ETB never triggers")
    void cannotTargetAngelYouControl() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.setHand(player1, List.of(new RestorationAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Opponent's non-Angel is not a legal target — ETB never triggers")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RestorationAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Flickering a stolen creature returns it under your control permanently")
    void flickersStolenCreatureUnderYourControl() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));

        harness.setHand(player1, List.of(new RestorationAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.stolenCreatures).containsEntry(returned.getId(), player2.getId());
    }
}
