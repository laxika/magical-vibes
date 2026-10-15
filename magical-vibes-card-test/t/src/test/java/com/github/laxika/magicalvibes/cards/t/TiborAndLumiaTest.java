package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DryadSophisticate;
import com.github.laxika.magicalvibes.cards.s.ScorchedRusalka;
import com.github.laxika.magicalvibes.cards.v.VertigoSpawn;
import com.github.laxika.magicalvibes.cards.w.WeeDragonauts;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TiborAndLumia.class, VertigoSpawn.class, DryadSophisticate.class,
        ScorchedRusalka.class, WeeDragonauts.class})
class TiborAndLumiaTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a blue spell lets Tibor and Lumia give a creature flying until end of turn")
    void blueSpellGrantsFlyingToTargetCreature() {
        harness.addToBattlefield(player1, new TiborAndLumia());
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new DryadSophisticate());
        harness.setHand(player1, List.of(new VertigoSpawn()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, dryad.getId());
        harness.passBothPriorities();

        assertThat(dryad.hasKeyword(Keyword.FLYING)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(dryad.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Casting a blue spell can target an opponent's creature")
    void blueSpellCanTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new TiborAndLumia());
        Permanent dryad = harness.addToBattlefieldAndReturn(player2, new DryadSophisticate());
        harness.setHand(player1, List.of(new VertigoSpawn()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, dryad.getId());
        harness.passBothPriorities();

        assertThat(dryad.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting a red spell damages creatures without flying")
    void redSpellDamagesCreaturesWithoutFlying() {
        Permanent tiborAndLumia = harness.addToBattlefieldAndReturn(player1, new TiborAndLumia());
        Permanent dryad = harness.addToBattlefieldAndReturn(player2, new DryadSophisticate());
        Permanent dragonauts = harness.addToBattlefieldAndReturn(player2, new WeeDragonauts());
        harness.setHand(player1, List.of(new ScorchedRusalka()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(tiborAndLumia.getMarkedDamage()).isEqualTo(1);
        assertThat(dryad.getMarkedDamage()).isEqualTo(1);
        assertThat(dragonauts.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An opponent's red spell does not trigger Tibor and Lumia")
    void opponentsRedSpellDoesNotTrigger() {
        Permanent tiborAndLumia = harness.addToBattlefieldAndReturn(player1, new TiborAndLumia());
        harness.setHand(player2, List.of(new ScorchedRusalka()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(tiborAndLumia.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A blue trigger cannot target a player")
    void blueTriggerCannotTargetPlayer() {
        harness.addToBattlefield(player1, new TiborAndLumia());
        harness.setHand(player1, List.of(new VertigoSpawn()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a green spell does not trigger either ability")
    void greenSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new TiborAndLumia());
        harness.setHand(player1, List.of(new DryadSophisticate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("A blue-red spell triggers both abilities in the controller's chosen order")
    void multicoloredSpellAllowsEitherTriggerOrder(boolean flyingFirst) {
        Permanent tiborAndLumia = harness.addToBattlefieldAndReturn(player1, new TiborAndLumia());
        harness.addToBattlefield(player2, new DryadSophisticate());
        harness.setHand(player1, List.of(new WeeDragonauts()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, tiborAndLumia.getId());

        PendingInteraction.ColorChoice order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        String bottomTriggerColor = flyingFirst ? "red" : "blue";
        harness.handleListChoice(player1, order.options().stream()
                .filter(option -> option.contains("cast a " + bottomTriggerColor + " spell"))
                .findFirst().orElseThrow());

        harness.passBothPriorities();
        assertThat(tiborAndLumia.hasKeyword(Keyword.FLYING)).isEqualTo(flyingFirst);
        assertThat(tiborAndLumia.getMarkedDamage()).isEqualTo(flyingFirst ? 0 : 1);

        harness.passBothPriorities();
        assertThat(tiborAndLumia.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(tiborAndLumia.getMarkedDamage()).isEqualTo(flyingFirst ? 0 : 1);
        harness.assertInGraveyard(player2, "Dryad Sophisticate");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wee Dragonauts");
    }

    @Test
    @DisplayName("The red trigger resolves before the creature spell that triggered it")
    void redCreatureSpellIsNotDamagedByItsOwnCastTrigger() {
        harness.addToBattlefield(player1, new TiborAndLumia());
        harness.addToBattlefield(player2, new DryadSophisticate());
        harness.setHand(player1, List.of(new ScorchedRusalka()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dryad Sophisticate");
        harness.assertNotOnBattlefield(player1, "Scorched Rusalka");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Scorched Rusalka");
        harness.assertNotInGraveyard(player1, "Scorched Rusalka");
    }

    @Test
    @DisplayName("An opponent's blue spell does not grant flying")
    void opponentsBlueSpellDoesNotTrigger() {
        Permanent tiborAndLumia = harness.addToBattlefieldAndReturn(player1, new TiborAndLumia());
        harness.setHand(player2, List.of(new VertigoSpawn()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(tiborAndLumia.hasKeyword(Keyword.FLYING)).isFalse();
    }
}
