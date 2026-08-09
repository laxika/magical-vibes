package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SigiledSwordOfValeronTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0, vigilance, and Knight subtype")
    void equippedCreatureGetsGrants() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSword(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, creature).grantedSubtypes()).contains(CardSubtype.KNIGHT);
    }

    @Test
    @DisplayName("Equipped creature's attack creates a tapped and attacking vigilant Knight")
    void attackTriggerCreatesAttackingKnight() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSword(player1);
        sword.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Knight"))
                .findFirst()
                .orElseThrow();
        assertThat(knight.isTapped()).isTrue();
        assertThat(knight.getCard().getSubtypes()).contains(CardSubtype.KNIGHT);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger does not fire while the Sword is unattached")
    void noTriggerWhenUnattached() {
        addCreatureReady(player1, new GrizzlyBears());
        addSword(player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Sigiled Sword of Valeron"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Knight"));
    }

    @Test
    @DisplayName("Equip {3} attaches the Sword to a creature you control")
    void equipAttachesToCreature() {
        Permanent sword = addSword(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addSword(Player player) {
        Permanent perm = new Permanent(new SigiledSwordOfValeron());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
