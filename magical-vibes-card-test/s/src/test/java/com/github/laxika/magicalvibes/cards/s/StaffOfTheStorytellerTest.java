package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CallTheCavalry;
import com.github.laxika.magicalvibes.cards.c.CuriousInquiry;
import com.github.laxika.magicalvibes.cards.e.EleshNornMotherOfMachines;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StaffOfTheStoryteller.class, Forest.class, CallTheCavalry.class, GrizzlyBears.class, CuriousInquiry.class, EleshNornMotherOfMachines.class})
class StaffOfTheStorytellerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying Spirit and gets a story counter for it")
    void entersAndTracksCreatureTokenCreation() {
        harness.setHand(player1, List.of(new StaffOfTheStoryteller()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent staff = findPermanent(player1, "Staff of the Storyteller");
        assertThat(staff.getCounterCount(CounterType.STORY)).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Removes a story counter to draw a card")
    void removesStoryCounterAndDraws() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        staff.setCounterCount(CounterType.STORY, 1);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(staff.getCounterCount(CounterType.STORY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(staff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not get a story counter for a noncreature token")
    void ignoresNoncreatureTokenCreation() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent inquiry = harness.addToBattlefieldAndReturn(player1, new CuriousInquiry());
        inquiry.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(staff.getCounterCount(CounterType.STORY)).isZero();
    }

    @Test
    @DisplayName("A batch of two creature tokens gives only one story counter")
    void triggersOnceForMultipleCreatureTokens() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        harness.setHand(player1, List.of(new CallTheCavalry()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Knight")).hasSize(2);
        assertThat(staff.getCounterCount(CounterType.STORY)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent creating creature tokens gives no story counter")
    void ignoresOpponentCreatureTokenCreation() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CallTheCavalry()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Knight")).hasSize(2);
        assertThat(staff.getCounterCount(CounterType.STORY)).isZero();
    }

    @Test
    @DisplayName("A nontoken creature entering gives no story counter")
    void ignoresNontokenCreatureEntering() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(staff.getCounterCount(CounterType.STORY)).isZero();
    }

    @Test
    @DisplayName("Drawing requires a story counter")
    void cannotActivateWithoutStoryCounter() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(staff.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The counter and tap costs are paid before the draw resolves")
    void paysCostsImmediatelyAndDrawsAfterSourceLeaves() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        staff.setCounterCount(CounterType.STORY, 2);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(staff.getCounterCount(CounterType.STORY)).isEqualTo(1);
        assertThat(staff.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(staff);
        gd.playerGraveyards.get(player1.getId()).add(staff.getCard());
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Elesh Norn does not duplicate the token creation trigger")
    void tokenCreationTriggerIsNotAnEnterTrigger() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        harness.addToBattlefield(player1, new EleshNornMotherOfMachines());
        harness.setHand(player1, List.of(new CallTheCavalry()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Knight")).hasSize(2);
        assertThat(staff.getCounterCount(CounterType.STORY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opposing Elesh Norn does not suppress the token creation trigger")
    void tokenCreationTriggerIsNotSuppressedByOpposingEleshNorn() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        harness.addToBattlefield(player2, new EleshNornMotherOfMachines());
        harness.setHand(player1, List.of(new CallTheCavalry()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Knight")).hasSize(2);
        assertThat(staff.getCounterCount(CounterType.STORY)).isEqualTo(1);
    }

    @Test
    @DisplayName("The enters ability creates a 1/1 flying Spirit")
    void createsFlyingOneOneSpirit() {
        harness.setHand(player1, List.of(new StaffOfTheStoryteller()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
    }
}
