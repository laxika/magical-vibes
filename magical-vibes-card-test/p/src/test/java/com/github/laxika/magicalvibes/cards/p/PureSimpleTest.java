package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.d.DeclarationOfNaught;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.o.OcularHalo;
import com.github.laxika.magicalvibes.cards.t.TransguildCourier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PureSimple.class, TransguildCourier.class, MistralCharger.class, Bonesplitter.class,
        AzoriusSignet.class, ProperBurial.class, OcularHalo.class, DeclarationOfNaught.class})
class PureSimpleTest extends BaseCardTest {

    private static final int PURE = 0;
    private static final int SIMPLE = 1;

    @Test
    @DisplayName("Pure destroys a target multicolored permanent")
    void pureDestroysTargetMulticoloredPermanent() {
        Permanent multicolored = harness.addToBattlefieldAndReturn(player2, new TransguildCourier());
        harness.addToBattlefield(player2, new MistralCharger());
        harness.setHand(player1, List.of(new PureSimple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, PURE, List.of(multicolored.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Transguild Courier");
        harness.assertInGraveyard(player2, "Transguild Courier");
        harness.assertOnBattlefield(player2, "Mistral Charger");
    }

    @Test
    @DisplayName("Pure cannot target a monocolored permanent")
    void pureCannotTargetMonocoloredPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        harness.setHand(player1, List.of(new PureSimple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, PURE, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("multicolored");
    }

    @Test
    @DisplayName("Pure cannot target a colorless permanent")
    void pureCannotTargetColorlessPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        harness.setHand(player1, List.of(new PureSimple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, PURE, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("multicolored");
    }

    @Test
    @DisplayName("Simple destroys all Auras and Equipment but leaves other permanents")
    void simpleDestroysAurasAndEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.addToBattlefield(player1, new AzoriusSignet());
        harness.addToBattlefield(player1, new ProperBurial());

        harness.setHand(player1, List.of(new OcularHalo()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new PureSimple()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalSorcery(player1, 0, SIMPLE, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ocular Halo");
        harness.assertInGraveyard(player1, "Bonesplitter");
        harness.assertOnBattlefield(player1, "Azorius Signet");
        harness.assertOnBattlefield(player1, "Proper Burial");
        harness.assertOnBattlefield(player1, "Mistral Charger");
    }

    @Test
    @DisplayName("Simple destroys opposing Auras and Equipment too")
    void simpleDestroysOpposingAurasAndEquipment() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        harness.addToBattlefield(player2, new Bonesplitter());
        harness.addToBattlefield(player2, new ProperBurial());

        harness.setHand(player1, List.of(new OcularHalo()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new PureSimple()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalSorcery(player1, 0, SIMPLE, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ocular Halo");
        harness.assertInGraveyard(player2, "Bonesplitter");
        harness.assertOnBattlefield(player2, "Proper Burial");
        harness.assertOnBattlefield(player2, "Mistral Charger");
    }

    @Test
    @DisplayName("Pure can destroy its controller's permanent without destroying Equipment")
    void pureCanDestroyOwnPermanentWithoutResolvingSimple() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TransguildCourier());
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.addToBattlefield(player2, new Bonesplitter());
        harness.setHand(player1, List.of(new PureSimple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, PURE, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Transguild Courier");
        harness.assertOnBattlefield(player1, "Bonesplitter");
        harness.assertOnBattlefield(player2, "Bonesplitter");
    }

    @Test
    @DisplayName("Simple can resolve with no Auras or Equipment and leaves multicolored permanents")
    void simpleCanResolveWithoutMatchingPermanents() {
        harness.addToBattlefield(player2, new TransguildCourier());
        harness.setHand(player1, List.of(new PureSimple()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, SIMPLE, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Transguild Courier");
        harness.assertInGraveyard(player1, "Pure // Simple");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declaration of Naught naming Pure can counter the Pure half")
    void pureHasOnlyItsOwnNameOnStack() {
        Permanent declaration = harness.addToBattlefieldAndReturn(player2, new DeclarationOfNaught());
        declaration.setChosenName("Pure");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TransguildCourier());
        PureSimple spell = new PureSimple();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castModalSorcery(player1, 0, PURE, List.of(target.getId()));
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, spell.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Transguild Courier");
        harness.assertInGraveyard(player1, "Pure // Simple");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declaration of Naught naming Simple can counter the Simple half")
    void simpleHasOnlyItsOwnNameOnStack() {
        Permanent declaration = harness.addToBattlefieldAndReturn(player2, new DeclarationOfNaught());
        declaration.setChosenName("Simple");
        harness.addToBattlefield(player2, new Bonesplitter());
        PureSimple spell = new PureSimple();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castModalSorcery(player1, 0, SIMPLE, List.of());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, spell.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Bonesplitter");
        harness.assertInGraveyard(player1, "Pure // Simple");
        assertThat(gd.stack).isEmpty();
    }
}
