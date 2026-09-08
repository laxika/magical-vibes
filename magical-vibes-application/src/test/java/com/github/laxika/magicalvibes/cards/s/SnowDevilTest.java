package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnowDevil.class, KjeldoranWarrior.class, Plains.class, SnowCoveredPlains.class})
class SnowDevilTest extends BaseCardTest {

    private Permanent enchantedWarrior() {
        Permanent warrior = addCreatureReady(player1, new KjeldoranWarrior());

        Permanent aura = new Permanent(new SnowDevil());
        aura.setAttachedTo(warrior.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return warrior;
    }

    private void addSnowLand() {
        harness.addToBattlefield(player1, new SnowCoveredPlains());
    }

    private void addSnowNonland() {
        Permanent snowPermanent = harness.addToBattlefieldAndReturn(player1, new KjeldoranWarrior());
        TestCards.mutableCard(snowPermanent).setSupertypes(EnumSet.of(CardSupertype.SNOW));
    }

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent warrior = enchantedWarrior();

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Blocking with snow land grants first strike")
    void blockingWithSnowLandGrantsFirstStrike() {
        Permanent warrior = enchantedWarrior();
        addSnowLand();
        warrior.setBlocking(true);

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Snow land is checked for the Aura controller")
    void snowLandIsCheckedForAuraController() {
        Permanent warrior = addCreatureReady(player2, new KjeldoranWarrior());

        Permanent aura = new Permanent(new SnowDevil());
        aura.setAttachedTo(warrior.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        addSnowLand();
        warrior.setBlocking(true);

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Blocking without snow land does not grant first strike")
    void blockingWithoutSnowLandNoFirstStrike() {
        Permanent warrior = enchantedWarrior();
        harness.addToBattlefield(player1, new Plains());
        warrior.setBlocking(true);

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A snow nonland permanent does not count as a snow land")
    void snowNonlandDoesNotGrantFirstStrike() {
        Permanent warrior = enchantedWarrior();
        addSnowNonland();
        warrior.setBlocking(true);

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Not blocking with snow land does not grant first strike")
    void notBlockingWithSnowLandNoFirstStrike() {
        Permanent warrior = enchantedWarrior();
        addSnowLand();

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new KjeldoranWarrior());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new SnowDevil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent land = findPermanent(player1, "Plains");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
