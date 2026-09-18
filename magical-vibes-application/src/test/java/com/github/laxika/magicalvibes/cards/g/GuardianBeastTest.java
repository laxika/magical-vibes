package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Aladdin;
import com.github.laxika.magicalvibes.cards.a.AnimateArtifact;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.m.MagusOfTheUnseen;
import com.github.laxika.magicalvibes.cards.m.ManaPrism;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.StealArtifact;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Aladdin.class, AnimateArtifact.class, Disenchant.class, GuardianBeast.class,
        MagusOfTheUnseen.class, ManaPrism.class, Spellbook.class, StealArtifact.class})
class GuardianBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped Guardian Beast makes your noncreature artifacts indestructible")
    void protectsArtifactsFromDestruction() {
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new ManaPrism());
        harness.addToBattlefield(player1, new GuardianBeast());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, prism.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Mana Prism")).isEqualTo(prism.getId());
    }

    @Test
    @DisplayName("Untapped Guardian Beast prevents Auras from enchanting your noncreature artifacts")
    void preventsAurasFromEnchantingArtifacts() {
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new ManaPrism());
        harness.addToBattlefield(player1, new GuardianBeast());
        harness.setHand(player2, List.of(new AnimateArtifact()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, prism.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Untapped Guardian Beast prevents opponents from gaining your noncreature artifacts")
    void preventsOpponentsFromGainingArtifacts() {
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new ManaPrism());
        harness.addToBattlefield(player1, new GuardianBeast());
        addCreatureReady(player2, new Aladdin());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, prism.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Mana Prism")).isEqualTo(prism.getId());
    }

    @Test
    @DisplayName("A tapped Guardian Beast no longer prevents control changes")
    void allowsGainWhenGuardianIsTapped() {
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new ManaPrism());
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GuardianBeast());
        guardian.tap();
        addCreatureReady(player2, new Aladdin());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, prism.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player2, "Mana Prism")).isEqualTo(prism.getId());
    }


    @Test
    @DisplayName("Untapped Guardian Beast protects your noncreature artifacts")
    void protectsControlledNoncreatureArtifacts() {
        harness.addToBattlefield(player1, new GuardianBeast());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        assertThat(gqs.hasKeyword(gd, ownArtifact, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingArtifact, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Protection ends when Guardian Beast becomes tapped")
    void protectionEndsWhenTapped() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GuardianBeast());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        guardian.tap();

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Protected artifacts cannot be enchanted by other Auras")
    void protectedArtifactsCannotBeEnchanted() {
        harness.addToBattlefield(player1, new GuardianBeast());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player2, List.of(new StealArtifact()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protected artifacts survive destruction")
    void protectedArtifactsSurviveDestruction() {
        harness.addToBattlefield(player1, new GuardianBeast());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, artifact.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Other players cannot gain control of protected artifacts")
    void otherPlayersCannotGainControl() {
        harness.addToBattlefield(player1, new GuardianBeast());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        addCreatureReady(player2, new MagusOfTheUnseen());
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }
}
