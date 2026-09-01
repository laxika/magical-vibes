package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.cards.g.GusthasScepter;
import com.github.laxika.magicalvibes.cards.h.HelmOfObedience;
import com.github.laxika.magicalvibes.cards.x.XenicPoltergeist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GorillaShaman.class, HelmOfObedience.class, AesthirGlider.class,
        GusthasScepter.class})
class GorillaShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target noncreature artifact with mana value X")
    void destroysNoncreatureArtifactWithManaValueX() {
        harness.addToBattlefield(player1, new GorillaShaman());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HelmOfObedience());

        harness.addMana(player1, ManaColor.RED, 9); // {X=4}{X=4}{1}
        harness.activateAbility(player1, 0, 4, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Helm of Obedience");
        harness.assertInGraveyard(player2, "Helm of Obedience");
    }

    @Test
    @DisplayName("Cannot target an artifact whose mana value does not equal X")
    void cannotTargetArtifactWithDifferentManaValue() {
        harness.addToBattlefield(player1, new GorillaShaman());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HelmOfObedience());

        harness.addMana(player1, ManaColor.RED, 7); // {X=3}{X=3}{1}

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player1, new GorillaShaman());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AesthirGlider());

        harness.addMana(player1, ManaColor.RED, 7); // {X=3}{X=3}{1}

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a noncreature artifact you control")
    void canTargetNoncreatureArtifactYouControl() {
        harness.addToBattlefield(player1, new GorillaShaman());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HelmOfObedience());

        harness.addMana(player1, ManaColor.RED, 9); // {X=4}{X=4}{1}
        harness.activateAbility(player1, 0, 4, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Helm of Obedience");
        harness.assertInGraveyard(player1, "Helm of Obedience");
    }

    @Test
    @DisplayName("Can choose X equal to zero for a zero-mana artifact")
    void canChooseZeroForZeroManaArtifact() {
        harness.addToBattlefield(player1, new GorillaShaman());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GusthasScepter());

        harness.addMana(player1, ManaColor.RED, 1); // {X=0}{X=0}{1}
        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gustha's Scepter");
        harness.assertInGraveyard(player2, "Gustha's Scepter");
    }

    @Test
    @DisplayName("Requires two payments of X plus one generic mana")
    void requiresTwoPaymentsOfX() {
        harness.addToBattlefield(player1, new GorillaShaman());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HelmOfObedience());

        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 4, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(XenicPoltergeist.class)
    @DisplayName("Does not destroy an artifact that becomes a creature before resolution")
    void doesNotDestroyArtifactThatBecomesCreatureBeforeResolution() {
        harness.addToBattlefield(player1, new GorillaShaman());
        addCreatureReady(player1, new XenicPoltergeist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HelmOfObedience());

        harness.addMana(player1, ManaColor.RED, 9); // {X=4}{X=4}{1}
        harness.activateAbility(player1, 0, 4, target.getId());
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Helm of Obedience");
        harness.assertNotInGraveyard(player2, "Helm of Obedience");
    }
}
