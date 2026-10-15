package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeneralFerrousRokiric.class, GrizzlyBears.class, Shock.class, WoollyThoctar.class})
class GeneralFerrousRokiricTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a multicolored spell creates a red and white Golem artifact creature")
    void multicoloredSpellCreatesGolem() {
        harness.addToBattlefield(player1, new GeneralFerrousRokiric());
        harness.setHand(player1, List.of(new WoollyThoctar()));
        addWoollyThoctarMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent golem = findPermanent(player1, "Golem");
        assertThat(golem.getCard().isToken()).isTrue();
        assertThat(golem.getCard().getPower()).isEqualTo(4);
        assertThat(golem.getCard().getToughness()).isEqualTo(4);
        assertThat(golem.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(golem.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(golem.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(golem.getCard().getSubtypes()).containsExactly(CardSubtype.GOLEM);
    }

    @Test
    @DisplayName("Monocolored spells and opponents' multicolored spells do not create Golems")
    void onlyControllerMulticoloredSpellsCreateGolems() {
        harness.addToBattlefield(player1, new GeneralFerrousRokiric());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Golem")).isEmpty();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WoollyThoctar()));
        addWoollyThoctarMana(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Golem")).isEmpty();
    }

    @Test
    @DisplayName("Monocolored spells controlled by an opponent cannot target General Ferrous Rokiric")
    void hasHexproofFromMonocolored() {
        Permanent general = harness.addToBattlefieldAndReturn(player2, new GeneralFerrousRokiric());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, general.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The controller can target the General with a monocolored spell")
    void controllerCanTargetWithMonocoloredSpell() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new GeneralFerrousRokiric());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, general.getId());

        harness.assertNotOnBattlefield(player1, "General Ferrous Rokiric");
        harness.assertInGraveyard(player1, "General Ferrous Rokiric");
        assertThat(findPermanents(player1, "Golem")).isEmpty();
    }

    @Test
    @DisplayName("Monocolored creatures can block the General and deal combat damage to it")
    void monocoloredCreatureCanBlockAndKillGeneral() {
        addCreatureReady(player1, new GeneralFerrousRokiric());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "General Ferrous Rokiric");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Hexproof does not prevent combat damage from a monocolored attacker")
    void monocoloredAttackerDealsCombatDamageToGeneral() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GeneralFerrousRokiric());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "General Ferrous Rokiric");
    }

    @Test
    @DisplayName("Casting the General itself does not trigger its battlefield ability")
    void castingGeneralDoesNotCreateGolem() {
        harness.setHand(player1, List.of(new GeneralFerrousRokiric()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "General Ferrous Rokiric");
        assertThat(findPermanents(player1, "Golem")).isEmpty();
    }

    @Test
    @DisplayName("The Golem is created before the multicolored creature spell resolves")
    void castTriggerResolvesBeforeCreatureSpell() {
        harness.addToBattlefield(player1, new GeneralFerrousRokiric());
        harness.setHand(player1, List.of(new WoollyThoctar()));
        addWoollyThoctarMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Golem")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Woolly Thoctar");

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Woolly Thoctar");
        assertThat(findPermanents(player1, "Golem")).hasSize(1);
    }

    private void addWoollyThoctarMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
