package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.o.OltecMatterweaver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AddMapTokenToArtifactTokenCreationEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WorldwalkerHelm.class, OltecMatterweaver.class})
class WorldwalkerHelmTest extends BaseCardTest {

    private static final String GNOME_MODE = "Create a 1/1 colorless Gnome artifact creature token";

    @Test
    void addsMapTokenWhenAnArtifactTokenIsCreated() {
        addHelmAndMatterweaver();

        castCreatureAndChooseGnome();

        assertThat(findPermanents(player1, "Gnome")).hasSize(1);
        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    void addsMapTokenWhenCopyingAnArtifactToken() {
        addHelmAndMatterweaver();
        castCreatureAndChooseGnome();
        Permanent gnome = findPermanent(player1, "Gnome");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, gnome.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gnome")).hasSize(2);
        assertThat(findPermanents(player1, "Map")).hasSize(2);
    }

    private void addHelmAndMatterweaver() {
        harness.addToBattlefield(player1, new WorldwalkerHelm());
        addCreatureReady(player1, new OltecMatterweaver());
    }

    private void castCreatureAndChooseGnome() {
        harness.setHand(player1, List.of(new OltecMatterweaver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, GNOME_MODE);
        resolveAllTriggers();
    }

    @Test
    void eachHelmAddsOneMapWithoutRecursing() {
        addHelmAndMatterweaver();
        harness.addToBattlefield(player1, new WorldwalkerHelm());

        castCreatureAndChooseGnome();

        assertThat(findPermanents(player1, "Gnome")).hasSize(1);
        assertThat(findPermanents(player1, "Map")).hasSize(2);
    }

    @Test
    void doesNotAddMapAfterLosingAbilities() {
        addHelmAndMatterweaver();
        findPermanent(player1, "Worldwalker Helm").setLosesAllAbilitiesUntilEndOfTurn(true);

        castCreatureAndChooseGnome();

        assertThat(findPermanents(player1, "Gnome")).hasSize(1);
        assertThat(findPermanents(player1, "Map")).isEmpty();
    }

    @Test
    void doesNotAddMapWhenReplacementAbilityIsSuppressed() {
        addHelmAndMatterweaver();
        findPermanent(player1, "Worldwalker Helm")
                .suppressStaticEffectUntilEndOfTurn(AddMapTokenToArtifactTokenCreationEffect.class);

        castCreatureAndChooseGnome();

        assertThat(findPermanents(player1, "Gnome")).hasSize(1);
        assertThat(findPermanents(player1, "Map")).isEmpty();
    }

    @Test
    void doesNotModifyOpponentsArtifactTokenCreation() {
        harness.addToBattlefield(player2, new WorldwalkerHelm());
        addCreatureReady(player1, new OltecMatterweaver());

        castCreatureAndChooseGnome();

        assertThat(findPermanents(player1, "Gnome")).hasSize(1);
        assertThat(findPermanents(player1, "Map")).isEmpty();
        assertThat(findPermanents(player2, "Map")).isEmpty();
    }

    @Test
    void canCopyMapAndAddsAnotherMap() {
        addHelmAndMatterweaver();
        castCreatureAndChooseGnome();
        Permanent map = findPermanent(player1, "Map");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, map.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Map")).hasSize(3);
        assertThat(findPermanent(player1, "Worldwalker Helm").isTapped()).isTrue();
    }

    @Test
    void copyDoesNotInheritCountersOrTappedState() {
        addHelmAndMatterweaver();
        castCreatureAndChooseGnome();
        Permanent gnome = findPermanent(player1, "Gnome");
        gnome.setTapped(true);
        gnome.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, gnome.getId());
        resolveAllTriggers();

        Permanent copy = findPermanents(player1, "Gnome").stream()
                .filter(permanent -> !permanent.getId().equals(gnome.getId())).findFirst().orElseThrow();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetNontokenArtifact() {
        harness.addToBattlefield(player1, new WorldwalkerHelm());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WorldwalkerHelm());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsArtifactToken() {
        addHelmAndMatterweaver();
        castCreatureAndChooseGnome();
        harness.addToBattlefield(player2, new WorldwalkerHelm());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null,
                findPermanent(player1, "Gnome").getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsNothingWhenTargetLeavesBeforeResolution() {
        addHelmAndMatterweaver();
        castCreatureAndChooseGnome();
        Permanent gnome = findPermanent(player1, "Gnome");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, gnome.getId());
        gd.playerBattlefields.get(player1.getId()).remove(gnome);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gnome")).isEmpty();
        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }
}
