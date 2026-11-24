package com.github.laxika.magicalvibes.service.effect;


import java.util.UUID;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeEnchantedByOtherAurasEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBecomeSuspectedEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveMinusOneMinusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromNonSubtypeCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.networking.model.GrantedAbilityView;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GrantedAbilityViewFactoryTest {

    private final GrantedAbilityViewFactory factory = new GrantedAbilityViewFactory();
    private final GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");

    @Test
    @DisplayName("Formats a granted protection effect and retains its source")
    void formatsGrantedProtectionWithSource() {
        ProtectionFromColorsEffect protection =
                new ProtectionFromColorsEffect(EnumSet.allOf(CardColor.class));
        Permanent creature = permanent("Colossus");

        List<GrantedAbilityView> views = factory.create(
                gameData, creature,
                bonus(Set.of(), List.of(protection)),
                List.of(new GrantedEffectAttribution("Favor of the Mighty", protection)), false);

        assertThat(views).containsExactly(
                new GrantedAbilityView("Protection from each color", "Favor of the Mighty"));
    }

    @Test
    @DisplayName("Formats every currently granted non-keyword effect shape")
    void formatsCurrentGrantedEffectShapes() {
        List<CardEffect> effects = List.of(
                new CantBeBlockedEffect(),
                TargetingRestrictionEffect.hexproof(),
                TargetingRestrictionEffect.spells(),
                new CantBeEnchantedByOtherAurasEffect(),
                new CantHaveMinusOneMinusOneCountersEffect());

        List<GrantedAbilityView> views =
                factory.create(gameData, permanent("Creature"), bonus(Set.of(), effects), List.of(), false);

        assertThat(views).extracting(GrantedAbilityView::text).containsExactly(
                "Can't be blocked",
                "Hexproof",
                "Can't be the target of spells",
                "Can't be enchanted by other Auras",
                "Can't have \u22121/\u22121 counters put on it");
    }

    @Test
    @DisplayName("Lists attachment-imposed restrictions that are attributed but not granted")
    void listsAttributedAttachmentRestrictions() {
        List<GrantedAbilityView> views = factory.create(
                gameData, permanent("Creature"),
                bonus(Set.of(), List.of()),
                List.of(new GrantedEffectAttribution("Assault Suit", new CantBeSacrificedEffect()),
                        new GrantedEffectAttribution("Airtight Alibi", new CantBecomeSuspectedEffect()),
                        new GrantedEffectAttribution("Anti-Magic Aura", new CantBeEnchantedByOtherAurasEffect())), false);

        assertThat(views).containsExactly(
                new GrantedAbilityView("Can't be sacrificed", "Assault Suit"),
                new GrantedAbilityView("Can't become suspected", "Airtight Alibi"),
                new GrantedAbilityView("Can't be enchanted by other Auras", "Anti-Magic Aura"));
    }

    @Test
    @DisplayName("Chosen and temporary protection are shown while printed protection is omitted")
    void projectsOnlyProtectionBeyondPrintedText() {
        Card card = creatureCard("Voice");
        card.addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));
        card.addStaticEffect(ProtectionFromColorsEffect.chosenColor());
        Permanent permanent = new Permanent(card);
        permanent.setChosenColor(CardColor.RED);

        List<GrantedAbilityView> views = factory.create(
                gameData, permanent,
                bonus(Set.of(CardColor.BLACK, CardColor.RED, CardColor.GREEN), List.of()),
                List.of(), false);

        assertThat(views).containsExactly(
                new GrantedAbilityView("Protection from red", "Voice"),
                new GrantedAbilityView("Protection from green", null));
    }

    @Test
    @DisplayName("Projects runtime unblockable and non-subtype protection")
    void projectsRuntimeGrantedAbilities() {
        Permanent permanent = permanent("Creature");

        List<GrantedAbilityView> views = factory.create(gameData, permanent,
                bonus(Set.of(), List.of(new ProtectionFromNonSubtypeCreaturesEffect(CardSubtype.HUMAN))),
                List.of(), true);

        assertThat(views).containsExactly(
                new GrantedAbilityView("Protection from non-Human creatures", null),
                new GrantedAbilityView("Can't be blocked", null));
    }

    @Test
    @DisplayName("Formats direct protection using its retained source")
    void formatsDirectProtectionWithSource() {
        ProtectionFromColorsEffect protection =
                new ProtectionFromColorsEffect(Set.of(CardColor.BLACK, CardColor.GREEN));

        List<GrantedAbilityView> views = factory.create(
                gameData, permanent("Creature"),
                bonus(Set.of(CardColor.BLACK, CardColor.GREEN), List.of()),
                List.of(new GrantedEffectAttribution(
                        "Sword of Feast and Famine", protection)), false);

        assertThat(views).containsExactly(new GrantedAbilityView(
                "Protection from black and green", "Sword of Feast and Famine"));
    }

    private Permanent permanent(String name) {
        return new Permanent(creatureCard(name));
    }

    private Card creatureCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private GameQueryService.StaticBonus bonus(
            Set<CardColor> protectionColors, List<CardEffect> grantedEffects) {
        return new GameQueryService.StaticBonus(
                0, 0, Set.of(), protectionColors, false, List.of(), grantedEffects,
                Set.of(), List.of(), Set.of(), Set.of(),
                false, false, false, false, Set.of(),
                false, 0, 0, false, false);
    }
}
