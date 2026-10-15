package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SealOfStrength;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KoglaAndYidaro.class, GrizzlyBears.class, Ornithopter.class, SealOfStrength.class})
class KoglaAndYidaroTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode gives Kogla and Yidaro trample and haste until end of turn")
    void gainsTrampleAndHasteUntilEndOfTurn() {
        castKogla(0, null);
        resolveCreatureAndEtb();

        Permanent kogla = findPermanent(player1, "Kogla and Yidaro");
        assertThat(kogla.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(kogla.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(kogla.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(kogla.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("ETB fight mode fights a creature the controller does not control")
    void fightsTargetCreatureYouDoNotControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castKogla(1, bears.getId());
        resolveCreatureAndEtb();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent kogla = findPermanent(player1, "Kogla and Yidaro");
        assertThat(kogla.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fight mode rejects a creature controlled by its controller")
    void fightModeRejectsOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> castKogla(1, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Hand ability destroys an artifact, shuffles Kogla and Yidaro, and draws")
    void handAbilityDestroysArtifactShufflesAndDraws() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new SealOfStrength()));
        harness.setHand(player1, List.of(new KoglaAndYidaro()));
        addAbilityMana();

        harness.activateHandAbility(player1, 0, artifact.getId());
        harness.assertInGraveyard(player1, "Kogla and Yidaro");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        harness.assertNotInGraveyard(player1, "Kogla and Yidaro");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card ->
                card.getName().equals("Kogla and Yidaro")
                        || card.getName().equals("Grizzly Bears")
                        || card.getName().equals("Seal of Strength"));
    }

    @Test
    @DisplayName("Hand ability can destroy an enchantment or choose no target")
    void handAbilityAcceptsEnchantmentAndNoTarget() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new SealOfStrength());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Ornithopter()));
        harness.setHand(player1, List.of(new KoglaAndYidaro()));
        addAbilityMana();

        harness.activateHandAbility(player1, 0, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Ornithopter()));
        harness.setHand(player1, List.of(new KoglaAndYidaro()));
        addAbilityMana();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Kogla and Yidaro");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An illegal chosen target prevents both the shuffle and the draw")
    void illegalHandAbilityTargetPreventsAllEffects() {
        Permanent seal = harness.addToBattlefieldAndReturn(player2, new SealOfStrength());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.setHand(player1, List.of(new KoglaAndYidaro()));
        addAbilityMana();

        harness.activateHandAbility(player1, 0, seal.getId());
        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kogla and Yidaro");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing no target shuffles the discarded card before drawing from an empty library")
    void noTargetWithEmptyLibraryDrawsTheShuffledCard() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new KoglaAndYidaro()));
        addAbilityMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Kogla and Yidaro");
        harness.assertInHand(player1, "Kogla and Yidaro");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Hand ability rejects a creature that is neither an artifact nor an enchantment")
    void handAbilityRejectsOrdinaryCreatureWithoutDiscarding() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KoglaAndYidaro()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Kogla and Yidaro");
        harness.assertNotInGraveyard(player1, "Kogla and Yidaro");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fight does not deal damage if Kogla and Yidaro leaves before the trigger resolves")
    void fightDoesNothingWithoutSourceOnBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castKogla(1, bears.getId());
        harness.passBothPriorities();
        Permanent kogla = findPermanent(player1, "Kogla and Yidaro");
        gd.playerBattlefields.get(player1.getId()).remove(kogla);
        harness.setExile(player1, List.of(kogla.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    private void castKogla(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new KoglaAndYidaro()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
