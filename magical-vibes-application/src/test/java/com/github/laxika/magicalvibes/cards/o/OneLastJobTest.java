package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OneLastJob.class, GrizzlyBears.class, HolyStrength.class, DarksteelAxe.class})
class OneLastJobTest extends BaseCardTest {

    @Test
    void returnsTargetCreature() {
        Card creature = new GrizzlyBears();
        addToGraveyard(player1, creature);

        cast(new int[]{0}, List.of(creature.getId()), 5);

        assertOnBattlefield(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void returnsTargetMountOrVehicle() {
        Card mount = new GrizzlyBears();
        mount.setSubtypes(List.of(CardSubtype.MOUNT));
        addToGraveyard(player1, mount);

        cast(new int[]{1}, List.of(mount.getId()), 4);

        assertOnBattlefield(mount.getId());
    }

    @Test
    void returnsAuraAttachedToControlledCreature() {
        Card aura = new HolyStrength();
        addToGraveyard(player1, aura);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        cast(new int[]{2}, List.of(aura.getId()), 4);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanent(aura.getId()).getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void returnsEquipmentAttachedToControlledCreature() {
        Card equipment = new DarksteelAxe();
        addToGraveyard(player1, equipment);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        cast(new int[]{2}, List.of(equipment.getId()), 4);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanent(equipment.getId()).getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void resolvesMultipleModesWithSeparateGraveyardTargets() {
        Card creature = new GrizzlyBears();
        Card mount = new GrizzlyBears();
        mount.setSubtypes(List.of(CardSubtype.MOUNT));
        addToGraveyard(player1, creature, mount);

        cast(new int[]{0, 1}, List.of(creature.getId(), mount.getId()), 6);

        assertOnBattlefield(creature.getId());
        assertOnBattlefield(mount.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void rejectsNonMountOrVehicleForSecondMode() {
        Card creature = new GrizzlyBears();
        addToGraveyard(player1, creature);

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(creature.getId()), 4))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<UUID> targetIds, int totalMana) {
        harness.setHand(player1, List.of(new OneLastJob()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 1);
        harness.castModalSorceryWithModes(player1, 0, 1, 3, modes, targetIds, List.of());
        harness.passBothPriorities();
    }

    private void addToGraveyard(com.github.laxika.magicalvibes.model.Player player, Card... cards) {
        harness.setGraveyard(player, List.of(cards));
    }

    private void assertOnBattlefield(UUID cardId) {
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(cardId));
    }

    private Permanent findPermanent(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
