package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AlchemistsApprentice;
import com.github.laxika.magicalvibes.cards.a.AngelicArmaments;
import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GallowsAtWillowHill.class, AlchemistsApprentice.class, AngelicArmaments.class, Vorstclaw.class})
class GallowsAtWillowHillTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the targeted creature and gives its controller a 1/1 white flying Spirit")
    void destroysTargetAndGivesSpirit() {
        Permanent gallows = harness.addToBattlefieldAndReturn(player1, new GallowsAtWillowHill());
        addHumans(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        tapHumans(player1, 3);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gallows.isTapped()).isTrue();
        assertThat(tappedHumanCount(player1)).isEqualTo(3);

        List<Permanent> spirits = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .toList();
        assertThat(spirits).hasSize(1);
        Permanent spirit = spirits.getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT));
    }

    @Test
    @DisplayName("A non-creature permanent cannot be targeted")
    void cannotTargetNonCreature() {
        harness.addToBattlefieldAndReturn(player1, new GallowsAtWillowHill());
        addHumans(player1, 4);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelicArmaments());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with fewer than three untapped Humans")
    void cannotActivateWithoutThreeHumans() {
        harness.addToBattlefieldAndReturn(player1, new GallowsAtWillowHill());
        addHumans(player1, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Human creatures cannot pay the tap cost")
    void nonHumansCannotPayCost() {
        harness.addToBattlefieldAndReturn(player1, new GallowsAtWillowHill());
        addHumans(player1, 2);
        harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Summoning-sick Humans can pay the cost, including the targeted Human")
    void canTapSummoningSickTargetHuman() {
        harness.addToBattlefield(player1, new GallowsAtWillowHill());
        addHumans(player1, 3);
        List<Permanent> humans = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof AlchemistsApprentice).toList();
        humans.forEach(p -> p.setSummoningSick(true));
        Permanent target = humans.getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        tapHumans(player1, 3);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(tappedHumanCount(player1)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT)).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Already tapped Humans do not count toward the activation cost")
    void tappedHumansCannotPayCost() {
        harness.addToBattlefield(player1, new GallowsAtWillowHill());
        addHumans(player1, 3);
        gd.playerBattlefields.get(player1.getId()).get(1).tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regenerating the target does not prevent its controller from creating a Spirit")
    void regeneratingTargetStillGivesSpirit() {
        harness.addToBattlefield(player1, new GallowsAtWillowHill());
        addHumans(player1, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        target.setRegenerationShield(1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        tapHumans(player1, 3);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT)).hasSize(1);
    }

    @Test
    @DisplayName("No Spirit is created when the only target leaves before resolution")
    void missingTargetDoesNotGiveSpirit() {
        harness.addToBattlefield(player1, new GallowsAtWillowHill());
        addHumans(player1, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlchemistsApprentice());
        harness.setLibrary(player2, List.of(new Vorstclaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        tapHumans(player1, 3);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(tappedHumanCount(player1)).isEqualTo(3);
    }

    private void addHumans(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new AlchemistsApprentice());
        }
    }

    private void tapHumans(Player player, int count) {
        List<Permanent> untapped = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.HUMAN))
                .filter(p -> !p.isTapped())
                .limit(count)
                .toList();
        for (Permanent human : untapped) {
            harness.handlePermanentChosen(player, human.getId());
        }
    }

    private long tappedHumanCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.HUMAN))
                .filter(Permanent::isTapped)
                .count();
    }
}
