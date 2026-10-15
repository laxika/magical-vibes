package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorSanctifiers.class, AngelicChorus.class, FountainOfYouth.class, GrizzlyBears.class})
class KorSanctifiersTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB ability does not trigger")
    void withoutKickerDoesNotTrigger() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new KorSanctifiers()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kor Sanctifiers");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When kicked, the ETB ability destroys an artifact")
    void kickedDestroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        castKicked(targetId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("When kicked, the ETB ability destroys an enchantment")
    void kickedDestroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        castKicked(targetId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KorSanctifiers()));
        addKickedMana();
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("Kicked Sanctifiers can destroy its controller's artifact")
    void kickedDestroysOwnArtifact() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        castKicked(harness.getPermanentId(player1, "Fountain of Youth"));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kor Sanctifiers");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Kicked Sanctifiers can enter when there are no legal targets")
    void kickedWithNoLegalTargets() {
        harness.setHand(player1, List.of(new KorSanctifiers()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kor Sanctifiers");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Unkicked Sanctifiers can enter when there are no legal targets")
    void unkickedWithNoLegalTargets() {
        harness.setHand(player1, List.of(new KorSanctifiers()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kor Sanctifiers");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Entering without being cast does not trigger the kicked ability")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.enterBattlefieldAndReturn(player1, new KorSanctifiers());

        harness.assertOnBattlefield(player1, "Kor Sanctifiers");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The kicked trigger resolves after Sanctifiers leaves the battlefield")
    void triggerResolvesWithoutSource() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        castKicked(harness.getPermanentId(player2, "Fountain of Youth"));
        var source = findPermanent(player1, "Kor Sanctifiers");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kor Sanctifiers");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    private void castKicked(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KorSanctifiers()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
