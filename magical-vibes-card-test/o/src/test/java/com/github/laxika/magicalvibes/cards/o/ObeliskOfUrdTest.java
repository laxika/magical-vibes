package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.g.GoblinRoughrider;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObeliskOfUrd.class, GoblinRoughrider.class, ElvishMystic.class})
class ObeliskOfUrdTest extends BaseCardTest {

    private Permanent putObeliskWithChosenType(CardSubtype subtype) {
        Permanent obelisk = harness.addToBattlefieldAndReturn(player1, new ObeliskOfUrd());
        obelisk.setChosenSubtype(subtype);
        return obelisk;
    }

    @Test
    @DisplayName("Resolving Obelisk of Urd prompts for a creature type and stores the choice")
    void castingPromptsForSubtypeChoice() {
        harness.setHand(player1, List.of(new ObeliskOfUrd()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GOBLIN");

        assertThat(findPermanent(player1, "Obelisk of Urd").getChosenSubtype()).isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Creatures you control of the chosen type get +2/+2")
    void boostsOwnCreaturesOfChosenType() {
        harness.addToBattlefield(player1, new GoblinRoughrider());
        putObeliskWithChosenType(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player1, "Goblin Roughrider"));
        assertThat(bonus.power()).isEqualTo(2);
        assertThat(bonus.toughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures of a different type are not boosted")
    void doesNotBoostOtherTypes() {
        harness.addToBattlefield(player1, new ElvishMystic());
        putObeliskWithChosenType(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player1, "Elvish Mystic"));
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent creatures of the chosen type are not boosted")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player2, new GoblinRoughrider());
        putObeliskWithChosenType(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player2, "Goblin Roughrider"));
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost disappears when Obelisk of Urd leaves the battlefield")
    void boostRemovedWhenObeliskLeaves() {
        harness.addToBattlefield(player1, new GoblinRoughrider());
        Permanent obelisk = putObeliskWithChosenType(CardSubtype.GOBLIN);

        Permanent goblin = findPermanent(player1, "Goblin Roughrider");
        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(obelisk);

        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(0);
    }

    @Test
    @DisplayName("Six summoning-sick creatures can convoke Obelisk without mana")
    void castsEntirelyWithConvoke() {
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new GoblinRoughrider()))
                .toList();
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setHand(player1, List.of(new ObeliskOfUrd()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allSatisfy(creature -> assertThat(creature.isTapped()).isTrue());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        harness.assertOnBattlefield(player1, "Obelisk of Urd");
        assertThat(creatures).allSatisfy(creature -> {
            var bonus = gqs.computeStaticBonus(gd, creature);
            assertThat(bonus.power()).isEqualTo(2);
            assertThat(bonus.toughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Convoke combines creatures of different colors with mana")
    void castsWithManaAndConvoke() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinRoughrider());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        harness.setHand(player1, List.of(new ObeliskOfUrd()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(goblin.getId(), elf.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        assertThat(goblin.isTapped()).isTrue();
        assertThat(elf.isTapped()).isTrue();
        assertThat(gqs.computeStaticBonus(gd, elf).power()).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Multiple Obelisks choosing the same type stack their bonuses")
    void bonusesStack() {
        putObeliskWithChosenType(CardSubtype.GOBLIN);
        putObeliskWithChosenType(CardSubtype.GOBLIN);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinRoughrider());

        var bonus = gqs.computeStaticBonus(gd, goblin);
        assertThat(bonus.power()).isEqualTo(4);
        assertThat(bonus.toughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Each Obelisk keeps its own choice and boosts creatures entering later")
    void choicesAreIndependentAndMatchAnyCreatureSubtype() {
        harness.setHand(player1, List.of(new ObeliskOfUrd(), new ObeliskOfUrd()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "KNIGHT");
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "DRUID");

        assertThat(findPermanents(player1, "Obelisk of Urd"))
                .extracting(Permanent::getChosenSubtype)
                .containsExactly(CardSubtype.KNIGHT, CardSubtype.DRUID);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinRoughrider());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        var goblinBonus = gqs.computeStaticBonus(gd, goblin);
        var elfBonus = gqs.computeStaticBonus(gd, elf);
        assertThat(goblinBonus.power()).isEqualTo(2);
        assertThat(goblinBonus.toughness()).isEqualTo(2);
        assertThat(elfBonus.power()).isEqualTo(2);
        assertThat(elfBonus.toughness()).isEqualTo(2);
    }
}
