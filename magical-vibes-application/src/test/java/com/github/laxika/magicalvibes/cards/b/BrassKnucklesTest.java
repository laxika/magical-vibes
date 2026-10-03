package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrassKnuckles.class, BrokersVeteran.class})
class BrassKnucklesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Brass Knuckles creates a token copy")
    void castingCreatesTokenCopy() {
        harness.setHand(player1, List.of(new BrassKnuckles()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        List<Permanent> brassKnuckles = findPermanents(player1, "Brass Knuckles");
        assertThat(brassKnuckles).hasSize(2);
        assertThat(brassKnuckles).anySatisfy(permanent ->
                assertThat(permanent.getCard().isToken()).isTrue());
        assertThat(brassKnuckles).anySatisfy(permanent ->
                assertThat(permanent.getCard().isToken()).isFalse());
    }

    @Test
    @DisplayName("Equipped creature gains double strike only with two attached Equipment")
    void equippedCreatureNeedsTwoEquipment() {
        Permanent creature = addCreatureReady(player1, new BrokersVeteran());
        Permanent brassKnuckles = harness.addToBattlefieldAndReturn(player1, new BrassKnuckles());
        Permanent secondKnuckles = harness.addToBattlefieldAndReturn(player1, new BrassKnuckles());
        brassKnuckles.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();

        secondKnuckles.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        brassKnuckles.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equip attaches Brass Knuckles to a creature you control")
    void equipAttachesToTargetCreature() {
        Permanent brassKnuckles = harness.addToBattlefieldAndReturn(player1, new BrassKnuckles());
        Permanent creature = addCreatureReady(player1, new BrokersVeteran());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(brassKnuckles.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The token copy can equip and grant double strike together with the original")
    void tokenCopyCanEquip() {
        Permanent creature = addCreatureReady(player1, new BrokersVeteran());
        harness.setHand(player1, List.of(new BrassKnuckles()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        List<Permanent> equipment = findPermanents(player1, "Brass Knuckles");
        Permanent token = equipment.stream().filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        Permanent original = equipment.stream().filter(p -> !p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getAttachedTo()).isNull();
        assertThat(original.getAttachedTo()).isNull();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token),
                0, null, creature.getId());
        resolveAllTriggers();
        assertThat(token.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(original),
                0, null, creature.getId());
        resolveAllTriggers();
        assertThat(original.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equipment controlled by another player counts, and detaching it removes double strike")
    void otherPlayersEquipmentCountsOnlyWhileAttached() {
        Permanent creature = addCreatureReady(player1, new BrokersVeteran());
        Permanent knuckles = harness.addToBattlefieldAndReturn(player1, new BrassKnuckles());
        Permanent otherEquipment = harness.addToBattlefieldAndReturn(player2, new BrassKnuckles());
        knuckles.setAttachedTo(creature.getId());
        otherEquipment.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        otherEquipment.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
