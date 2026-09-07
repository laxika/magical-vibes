package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GroomsFinery.class, GrizzlyBears.class})
class GroomsFineryTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPlusZero() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent finery = addFineryReady(player1);
        finery.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void brideGownAttachedToCreatureYouControlAddsToughnessAndDeathtouch() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent finery = addFineryReady(player1);
        finery.setAttachedTo(creature.getId());

        Permanent gown = addEquipment(player2, "Bride's Gown");
        gown.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void brideGownAttachedToOpponentCreatureDoesNotEnableBonus() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent finery = addFineryReady(player1);
        finery.setAttachedTo(creature.getId());

        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent gown = addEquipment(player2, "Bride's Gown");
        gown.setAttachedTo(opponentCreature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void differentlyNamedEquipmentDoesNotEnableBonus() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent finery = addFineryReady(player1);
        finery.setAttachedTo(creature.getId());

        Permanent otherEquipment = addEquipment(player1, "Other Equipment");
        otherEquipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void equipAbilityAttachesToCreatureYouControl() {
        Permanent finery = addFineryReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(finery.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addFineryReady(Player player) {
        Permanent permanent = new Permanent(new GroomsFinery());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addEquipment(Player player, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
