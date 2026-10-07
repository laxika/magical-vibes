package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CacklingCounterpart;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Standardize;
import com.github.laxika.magicalvibes.cards.v.VampireNoble;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimotharBaronOfBats.class, VampireNoble.class, GrizzlyBears.class, Shock.class,
        Standardize.class, CacklingCounterpart.class})
class TimotharBaronOfBatsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying exiles a dying Vampire and creates a flying Bat")
    void payingExilesVampireAndCreatesBat() {
        addTimotharAndVampire();
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID vampireCardId = findPermanent(player1, "Vampire Noble").getCard().getId();
        killCreatureWithShock(findPermanent(player1, "Vampire Noble").getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(vampireCardId);
        assertThat(findPermanents(player1, "Bat")).hasSize(1);
        Permanent bat = findPermanent(player1, "Bat");
        assertThat(bat.getCard().getPower()).isEqualTo(1);
        assertThat(bat.getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bat, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining leaves the dying Vampire in its graveyard")
    void decliningLeavesVampireInGraveyard() {
        addTimotharAndVampire();

        killCreatureWithShock(findPermanent(player1, "Vampire Noble").getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vampire Noble");
        assertThat(findPermanents(player1, "Bat")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability ignores non-Vampire deaths")
    void ignoresNonVampireDeaths() {
        harness.addToBattlefield(player1, new TimotharBaronOfBats());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killCreatureWithShock(bears.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Bat")).isEmpty();
    }

    @Test
    @DisplayName("A Bat hit sacrifices it and returns the exiled Vampire tapped")
    void batCombatDamageReturnsExiledVampireTapped() {
        addTimotharAndVampire();
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent vampire = findPermanent(player1, "Vampire Noble");

        killCreatureWithShock(vampire.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent bat = findPermanent(player1, "Bat");
        bat.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bat)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).isEmpty();
        assertThat(findPermanents(player1, "Vampire Noble")).hasSize(1);
        assertThat(findPermanent(player1, "Vampire Noble").isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void wardCountersSpellWhenOpponentCannotDiscard() {
        Permanent timothar = harness.addToBattlefieldAndReturn(player1, new TimotharBaronOfBats());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, timothar.getId());
        resolveAllTriggers();

        assertThat(timothar.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void wardAllowsSpellAfterDiscarding() {
        Permanent timothar = harness.addToBattlefieldAndReturn(player1, new TimotharBaronOfBats());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, timothar.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(timothar.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void returnsOpponentOwnedVampireUnderBatControllersControl() {
        harness.addToBattlefield(player1, new TimotharBaronOfBats());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireNoble());
        gd.stolenCreatures.put(vampire.getId(), player2.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        killCreatureWithShock(vampire.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .containsExactly(vampire.getCard().getId());
        dealBatCombatDamage(findPermanent(player1, "Bat"));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Vampire Noble");
        harness.assertNotOnBattlefield(player2, "Vampire Noble");
        assertThat(findPermanent(player1, "Vampire Noble").isTapped()).isTrue();
    }

    @Test
    void returnsExiledVampireEvenIfBatDiesBeforeItsTriggerResolves() {
        Permanent bat = createBat();
        dealBatCombatDamage(bat);
        assertThat(gd.stack).isNotEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, bat));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Vampire Noble");
        assertThat(findPermanent(player1, "Vampire Noble").isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotPayWhenDyingCardHasAlreadyLeftGraveyard() {
        addTimotharAndVampire();
        Permanent vampire = findPermanent(player1, "Vampire Noble");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, vampire));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(vampire.getCard()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(findPermanents(player1, "Bat")).isEmpty();
        harness.assertInHand(player1, "Vampire Noble");
    }

    @Test
    void usesCreatureTypeImmediatelyBeforeDeath() {
        harness.addToBattlefield(player1, new TimotharBaronOfBats());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Standardize(), "{U}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.VAMPIRE.name());
        harness.addMana(player1, ManaColor.BLACK, 1);

        killCreatureWithShock(bears.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(bears.getCard().getId());
    }

    @Test
    void ignoresPrintedVampireThatWasNotVampireAtDeath() {
        addTimotharAndVampire();
        harness.castFromHand(player1, new Standardize(), "{U}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        killCreatureWithShock(findPermanent(player1, "Vampire Noble").getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Vampire Noble");
    }

    @Test
    void copiedBatSacrificesItselfWithoutReturningOriginalsExiledCard() {
        Permanent original = createBat();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, original.getId());
        Permanent copy = findPermanents(player1, "Bat").stream()
                .filter(bat -> !bat.getId().equals(original.getId())).findFirst().orElseThrow();

        dealBatCombatDamage(copy);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).containsExactly(original);
        harness.assertNotOnBattlefield(player1, "Vampire Noble");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    void sacrificesBatEvenWhenLinkedCardIsNoLongerExiled() {
        Permanent bat = createBat();
        Card vampire = gd.getPlayerExiledCards(player1.getId()).getFirst();
        gd.removeFromExile(vampire.getId());
        harness.setHand(player1, List.of(vampire));

        dealBatCombatDamage(bat);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).isEmpty();
        harness.assertNotOnBattlefield(player1, "Vampire Noble");
        harness.assertInHand(player1, "Vampire Noble");
    }

    @Test
    void ignoresOpponentsVampireDeath() {
        harness.addToBattlefield(player1, new TimotharBaronOfBats());
        Permanent vampire = harness.addToBattlefieldAndReturn(player2, new VampireNoble());
        killCreatureWithShock(vampire.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Bat")).isEmpty();
    }

    @Test
    void ignoresTokenVampireDeath() {
        addTimotharAndVampire();
        Permanent original = findPermanent(player1, "Vampire Noble");
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, original.getId());
        Permanent token = findPermanents(player1, "Vampire Noble").stream()
                .filter(vampire -> !vampire.getId().equals(original.getId())).findFirst().orElseThrow();

        killCreatureWithShock(token.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Vampire Noble")).containsExactly(original);
        assertThat(findPermanents(player1, "Bat")).isEmpty();
    }

    @Test
    void ignoresTimotharsOwnDeath() {
        Permanent timothar = harness.addToBattlefieldAndReturn(player1, new TimotharBaronOfBats());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, timothar));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Timothar, Baron of Bats");
        assertThat(findPermanents(player1, "Bat")).isEmpty();
    }

    private Permanent createBat() {
        addTimotharAndVampire();
        harness.addMana(player1, ManaColor.BLACK, 1);
        killCreatureWithShock(findPermanent(player1, "Vampire Noble").getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        return findPermanent(player1, "Bat");
    }

    private void dealBatCombatDamage(Permanent bat) {
        harness.forceActivePlayer(player1);
        bat.setAttacking(true);
        harness.resolveCombatDamage();
    }

    private void addTimotharAndVampire() {
        harness.addToBattlefield(player1, new TimotharBaronOfBats());
        harness.addToBattlefield(player1, new VampireNoble());
    }

    private void killCreatureWithShock(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
        if (gd.interaction.activeInteraction() == null) {
            harness.passBothPriorities();
        }
    }
}
