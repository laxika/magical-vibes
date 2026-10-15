package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.d.Deflection;
import com.github.laxika.magicalvibes.cards.d.DepartTheRealm;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheVoid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrvarTheAllForm.class, Distress.class, GrizzlyBears.class, IntoTheVoid.class,
        Deflection.class, DepartTheRealm.class, Island.class})
class OrvarTheAllFormTest extends BaseCardTest {

    @Test
    @DisplayName("Copies one of multiple other permanents targeted by your spell")
    void copiesOneOfMultipleOtherPermanentsTargetedBySpell() {
        harness.addToBattlefield(player1, new OrvarTheAllForm());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(firstTarget.getId(), secondTarget.getId()));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstTarget.getId(), secondTarget.getId());

        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears")
                        && permanent.getCard().isToken())
                .hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creates a token copy when an opponent discards Orvar")
    void createsTokenCopyWhenOpponentDiscardsOrvar() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new OrvarTheAllForm()));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears")
                        && permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when your spell targets only an opponent's permanent")
    void doesNotTriggerForOpponentsPermanent() {
        harness.addToBattlefield(player1, new OrvarTheAllForm());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, List.of(opponentTarget.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    void doesNotTriggerWhenSpellTargetsOnlyOrvar() {
        Permanent orvar = harness.addToBattlefieldAndReturn(player1, new OrvarTheAllForm());
        harness.setHand(player1, List.of(new DepartTheRealm()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, orvar.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Orvar, the All-Form");
        harness.assertInHand(player1, "Orvar, the All-Form");
    }

    @Test
    void createsCopyBeforeInstantReturnsOriginalToHand() {
        harness.addToBattlefield(player1, new OrvarTheAllForm());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DepartTheRealm()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
                    assertThat(token.getId()).isNotEqualTo(target.getId());
                });
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void doesNotCopyWhenSpellIsRetargetedToOpponentsPermanent() {
        harness.addToBattlefield(player1, new OrvarTheAllForm());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent replacement = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DepartTheRealm spell = new DepartTheRealm();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Deflection()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, original.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.handlePermanentChosen(player2, replacement.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void doesNotCopyWhenTargetLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new OrvarTheAllForm());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DepartTheRealm()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new DepartTheRealm()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void discardTriggerCanCopyOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player2, List.of(new OrvarTheAllForm()));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(land.getId());
        harness.handlePermanentChosen(player2, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getName()).isEqualTo("Island");
                });
    }

    @Test
    void discardTriggerDoesNotOfferPlayersAsTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new OrvarTheAllForm()));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).isEmpty();
    }
}
