package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.j.JasperaSentinel;
import com.github.laxika.magicalvibes.cards.m.Mistwalker;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReflectionsOfLittjara.class, JasperaSentinel.class, Mistwalker.class, NamelessInversion.class})
class ReflectionsOfLittjaraTest extends BaseCardTest {

    @Test
    @DisplayName("A matching creature spell is copied as a token")
    void matchingCreatureSpellIsCopiedAsToken() {
        addReflectionsChoosing(CardSubtype.ELF);
        harness.setHand(player1, List.of(creature("Test Elf", CardSubtype.ELF)));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> elves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Test Elf"))
                .toList();
        assertThat(elves).hasSize(2);
        assertThat(elves).anySatisfy(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
        assertThat(elves).anySatisfy(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }

    @Test
    @DisplayName("A non-creature spell of the chosen type also triggers")
    void matchingNonCreatureSpellAlsoTriggers() {
        addReflectionsChoosing(CardSubtype.ELF);
        harness.setHand(player1, List.of(sorcery("Test Tribal Spell", CardSubtype.ELF)));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("A spell of another type is not copied")
    void differentTypeDoesNotTrigger() {
        addReflectionsChoosing(CardSubtype.ELF);
        harness.setHand(player1, List.of(creature("Test Goblin", CardSubtype.GOBLIN)));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private void addReflectionsChoosing(CardSubtype subtype) {
        Permanent reflections = harness.addToBattlefieldAndReturn(player1, new ReflectionsOfLittjara());
        reflections.setChosenSubtype(subtype);
    }

    @Test
    @DisplayName("The type chosen on entry determines which spells are copied")
    void chosenTypeOnEntryCopiesMatchingSpell() {
        harness.setHand(player1, List.of(new ReflectionsOfLittjara(), new JasperaSentinel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Jaspera Sentinel")))
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Changeling spells match the chosen creature type")
    void changelingSpellIsCopied() {
        addReflectionsChoosing(CardSubtype.ELF);
        harness.setHand(player1, List.of(new Mistwalker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mistwalker")))
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's matching spell is not copied")
    void opponentsSpellIsNotCopied() {
        addReflectionsChoosing(CardSubtype.ELF);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new JasperaSentinel()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Copies of targeted Kindred spells retain their targets without a choice")
    void targetedKindredCopyDoesNotOfferNewTargets() {
        addReflectionsChoosing(CardSubtype.ELF);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mistwalker());
        harness.addToBattlefield(player2, new JasperaSentinel());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(target.getId());
    }

    private Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setSubtypes(List.of(subtype));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card sorcery(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.SORCERY);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
