package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.SramSeniorEdificer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FierceGuardianship.class, SramSeniorEdificer.class, Opt.class})
class FierceGuardianshipTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast without paying its mana cost while controlling a commander")
    void freeCastWhileControllingCommander() {
        addCommanderToBattlefield();
        Opt target = new Opt();
        harness.setHand(player2, List.of(new FierceGuardianship()));
        harness.castFromHand(player1, target, "{U}");
        harness.passPriority(player1);
        harness.castInstantWithAlternateCost(player2, 0, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Opt");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the free alternate cost without controlling a commander")
    void freeCastRequiresCommander() {
        Opt target = new Opt();
        harness.setHand(player1, List.of(new FierceGuardianship()));
        harness.castFromHand(player2, target, "{U}");

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
        harness.assertInHand(player1, "Fierce Guardianship");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        SramSeniorEdificer creature = new SramSeniorEdificer();
        harness.setHand(player2, List.of(new FierceGuardianship()));
        addCommanderToBattlefield();
        harness.castFromHand(player1, creature, "{1}{W}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, creature.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature spell");
    }

    private void addCommanderToBattlefield() {
        SramSeniorEdificer commander = new SramSeniorEdificer();
        gd.makeCommander(player2.getId(), commander);
        harness.addToBattlefield(player2, commander);
    }

    @Test
    void countersNoncreatureSpell() {
        Opt opt = new Opt();
        harness.setHand(player2, List.of(new FierceGuardianship()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castFromHand(player1, opt, "{U}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, opt.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Opt");
        harness.assertInGraveyard(player2, "Fierce Guardianship");
    }

    @Test
    void commanderAllowsCastingWithoutPayingManaCost() {
        SramSeniorEdificer commander = new SramSeniorEdificer();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);

        Opt opt = new Opt();
        harness.setHand(player1, List.of(new FierceGuardianship()));
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, opt, "{U}");
        harness.passPriority(player2);
        harness.castInstantWithAlternateCost(player1, 0, opt.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Fierce Guardianship");
        harness.assertInGraveyard(player2, "Opt");
    }

    @Test
    void cannotUseFreeCastWithoutControllingRegisteredCommander() {
        SramSeniorEdificer commander = new SramSeniorEdificer();
        gd.makeCommander(player1.getId(), commander);
        addToCommandZone(player1, commander);
        Opt target = new Opt();
        harness.setHand(player1, List.of(new FierceGuardianship()));
        harness.castFromHand(player2, target, "{U}");

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentsCommanderAllowsFreeCastWhileUnderYourControl() {
        SramSeniorEdificer commander = new SramSeniorEdificer();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player2, commander);
        Opt target = new Opt();
        harness.setHand(player2, List.of(new FierceGuardianship()));
        harness.castFromHand(player1, target, "{U}");
        harness.passPriority(player1);

        harness.castInstantWithAlternateCost(player2, 0, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Opt");
        harness.assertInGraveyard(player2, "Fierce Guardianship");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void ordinaryLegendaryCreatureDoesNotAllowFreeCast() {
        harness.addToBattlefield(player2, new SramSeniorEdificer());
        Opt target = new Opt();
        harness.setHand(player2, List.of(new FierceGuardianship()));
        harness.castFromHand(player1, target, "{U}");

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
        harness.assertInHand(player2, "Fierce Guardianship");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void commanderControlledByOpponentDoesNotAllowFreeCast() {
        SramSeniorEdificer commander = new SramSeniorEdificer();
        gd.makeCommander(player2.getId(), commander);
        harness.addToBattlefield(player1, commander);
        Opt target = new Opt();
        harness.setHand(player2, List.of(new FierceGuardianship()));
        harness.castFromHand(player1, target, "{U}");

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
        harness.assertInHand(player2, "Fierce Guardianship");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canCounterYourOwnNoncreatureSpell() {
        addCommanderToBattlefield();
        Opt target = new Opt();
        FierceGuardianship guardianship = new FierceGuardianship();
        harness.castFromHand(player2, target, "{U}");
        harness.setHand(player2, List.of(guardianship));
        harness.castInstantWithAlternateCost(player2, 0, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target, guardianship);
        assertThat(gd.stack).isEmpty();
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }
}
