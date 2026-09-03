package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({BrassKnuckles.class, GrizzlyBears.class, LeoninScimitar.class})
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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent brassKnuckles = addEquipmentReady(player1, new BrassKnuckles());
        Permanent scimitar = addEquipmentReady(player1, new LeoninScimitar());
        brassKnuckles.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();

        scimitar.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        brassKnuckles.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equip attaches Brass Knuckles to a creature you control")
    void equipAttachesToTargetCreature() {
        Permanent brassKnuckles = addEquipmentReady(player1, new BrassKnuckles());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(brassKnuckles.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addEquipmentReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
