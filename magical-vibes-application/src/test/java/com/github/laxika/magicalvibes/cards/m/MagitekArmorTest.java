package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.t.TownGreeter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagitekArmor.class, TownGreeter.class})
class MagitekArmorTest extends BaseCardTest {

    @Test
    void enteringCreatesColorlessHeroToken() {
        harness.setHand(player1, List.of(new MagitekArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> heroes = findPermanents(player1, "Hero").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(heroes).hasSize(1);
        Permanent hero = heroes.getFirst();
        assertThat(hero.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(hero.getCard().getColor()).isNull();
        assertThat(hero.getCard().getSubtypes()).containsExactly(CardSubtype.HERO);
        assertThat(hero.getCard().getPower()).isEqualTo(1);
        assertThat(hero.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    void crewOneAnimatesArmorAndTapsTheCrew() {
        Permanent armor = addArmorReady(player1);
        Permanent crew = addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, armor)).isTrue();
        assertThat(gqs.getEffectivePower(gd, armor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, armor)).isEqualTo(4);
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addArmorReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MagitekArmor());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TownGreeter());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    void freshlyCreatedHeroCanCrewImmediatelyAndAnimationExpires() {
        harness.setHand(player1, List.of(new MagitekArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent armor = findPermanent(player1, "Magitek Armor");
        Permanent hero = findPermanent(player1, "Hero");
        assertThat(gqs.isCreature(gd, armor)).isFalse();
        assertThat(hero.isSummoningSick()).isTrue();

        harness.activateAbility(player1, 0, null, null);
        assertThat(hero.isTapped()).isTrue();
        assertThat(armor.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, armor)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, armor)).isTrue();
        assertThat(armor.isSummoningSick()).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gqs.isCreature(gd, armor)).isFalse();
        assertThat(findPermanents(player1, "Hero")).containsExactly(hero);
    }

    @Test
    void tappedCreaturesCannotPayCrewCost() {
        Permanent armor = addArmorReady(player1);
        Permanent crew = addCreatureReady(player1);
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, armor)).isFalse();
    }

    @Test
    void opponentsCreaturesCannotPayCrewCost() {
        Permanent armor = addArmorReady(player1);
        Permanent opposingCreature = addCreatureReady(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, armor)).isFalse();
    }
}
