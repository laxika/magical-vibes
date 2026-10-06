package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShiningArmor.class, YouthfulKnight.class, Gingerbrute.class})
class ShiningArmorTest extends BaseCardTest {

    @Test
    void entersAttachedToTargetKnightYouControl() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        int initialToughness = gqs.getEffectiveToughness(gd, knight);
        harness.setHand(player1, List.of(new ShiningArmor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0, knight.getId());
        resolveAllTriggers();

        Permanent armor = findPermanent(player1, "Shining Armor");
        assertThat(armor.getAttachedTo()).isEqualTo(knight.getId());
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(initialToughness + 2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void canBeCastWithoutKnightAndEntersUnattached() {
        harness.addToBattlefield(player1, new Gingerbrute());
        harness.setHand(player1, List.of(new ShiningArmor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Shining Armor").getAttachedTo()).isNull();
    }

    @Test
    void rejectsNonKnightEtbTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.setHand(player1, List.of(new ShiningArmor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Knight");
    }

    @Test
    void equipGrantsToughnessAndVigilanceToAnyCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        int initialToughness = gqs.getEffectiveToughness(gd, creature);
        harness.addToBattlefield(player1, new ShiningArmor());
        harness.addMana(player1, ManaColor.WHITE, 3);

        int armorIndex = findPermanentIndex(player1, "Shining Armor");
        harness.activateAbility(player1, armorIndex, null, creature.getId());
        harness.passBothPriorities();

        Permanent armor = findPermanent(player1, "Shining Armor");
        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(initialToughness + 2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void canBeCastDuringOpponentsTurn() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShiningArmor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0, knight.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Shining Armor").getAttachedTo()).isEqualTo(knight.getId());
    }

    @Test
    void rejectsOpponentsKnightAsEnterTarget() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        harness.setHand(player1, List.of(new ShiningArmor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enterTriggerDoesNotAttachAfterTargetLeaves() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        harness.setHand(player1, List.of(new ShiningArmor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castArtifact(player1, 0, knight.getId());
        harness.passBothPriorities();

        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, knight);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Shining Armor").getAttachedTo()).isNull();
        harness.assertInGraveyard(player1, "Youthful Knight");
    }

    @Test
    void reequippingMovesBonusesToNewCreature() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        int knightToughness = gqs.getEffectiveToughness(gd, knight);
        int creatureToughness = gqs.getEffectiveToughness(gd, creature);
        int creaturePower = gqs.getEffectivePower(gd, creature);
        harness.setHand(player1, List.of(new ShiningArmor()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castArtifact(player1, 0, knight.getId());
        resolveAllTriggers();

        harness.activateAbility(player1, findPermanentIndex(player1, "Shining Armor"), null, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Shining Armor").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(knightToughness);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(creatureToughness + 2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(creaturePower);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());
        harness.addToBattlefield(player1, new ShiningArmor());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                findPermanentIndex(player1, "Shining Armor"), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashDoesNotAllowEquippingDuringUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.addToBattlefield(player1, new ShiningArmor());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                findPermanentIndex(player1, "Shining Armor"), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    private int findPermanentIndex(com.github.laxika.magicalvibes.model.Player player, String name) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(name)) {
                return i;
            }
        }
        throw new AssertionError("Permanent not found: " + name);
    }
}
