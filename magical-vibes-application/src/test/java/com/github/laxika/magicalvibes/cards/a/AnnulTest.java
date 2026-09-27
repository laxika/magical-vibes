package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlinkmothUrn;
import com.github.laxika.magicalvibes.cards.n.NuisanceEngine;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.s.SeethingSong;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Annul.class, AuriokBladewarden.class, BlinkmothUrn.class, NuisanceEngine.class,
        RuleOfLaw.class, SeethingSong.class})
class AnnulTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an artifact spell")
    void countersArtifactSpell() {
        NuisanceEngine nuisanceEngine = new NuisanceEngine();
        harness.castFromHand(player1, nuisanceEngine, "{3}");

        harness.setHand(player2, List.of(new Annul()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, nuisanceEngine.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nuisance Engine");
        harness.assertNotOnBattlefield(player1, "Nuisance Engine");
        harness.assertInGraveyard(player2, "Annul");
    }

    @Test
    @DisplayName("Counters an enchantment spell")
    void countersEnchantmentSpell() {
        RuleOfLaw ruleOfLaw = new RuleOfLaw();
        harness.castFromHand(player1, ruleOfLaw, "{2}{W}");

        harness.setHand(player2, List.of(new Annul()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, ruleOfLaw.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rule of Law");
        harness.assertNotOnBattlefield(player1, "Rule of Law");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        AuriokBladewarden bladewarden = new AuriokBladewarden();
        harness.castFromHand(player1, bladewarden, "{1}{W}");

        harness.setHand(player2, List.of(new Annul()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bladewarden.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an instant spell")
    void cannotTargetInstantSpell() {
        SeethingSong seethingSong = new SeethingSong();
        harness.castFromHand(player1, seethingSong, "{2}{R}");

        harness.setHand(player2, List.of(new Annul()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, seethingSong.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot target an activated ability from an artifact")
    void cannotTargetActivatedAbility() {
        NuisanceEngine nuisanceEngine = new NuisanceEngine();
        harness.addToBattlefield(player1, nuisanceEngine);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Annul()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, nuisanceEngine.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot target a triggered ability from an artifact")
    void cannotTargetTriggeredAbility() {
        BlinkmothUrn blinkmothUrn = new BlinkmothUrn();
        harness.addToBattlefield(player1, blinkmothUrn);

        harness.setHand(player2, List.of(new Annul()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        advanceToUpkeep(player1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, blinkmothUrn.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }
}
