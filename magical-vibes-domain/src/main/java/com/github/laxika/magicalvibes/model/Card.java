package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Card {

    private final UUID id = UUID.randomUUID();
    private final String name;
    private final CardType type;
    private final String manaCost;
    private final CardColor color;

    @Setter private Set<CardSupertype> supertypes = Set.of();
    @Setter private List<CardSubtype> subtypes = List.of();
    @Setter private String cardText;
    @Setter private Integer power;
    @Setter private Integer toughness;
    @Setter private Set<Keyword> keywords = Set.of();
    @Setter private boolean needsTarget;
    @Setter private boolean needsSpellTarget;
    @Setter private TargetFilter targetFilter;
    @Setter private String setCode;
    @Setter private String collectorNumber;
    @Setter private String flavorText;
    @Setter private String artist;
    @Setter private CardRarity rarity;

    private Map<EffectSlot, List<CardEffect>> effects = new EnumMap<>(EffectSlot.class);
    private List<ActivatedAbility> activatedAbilities = new ArrayList<>();

    public List<CardEffect> getEffects(EffectSlot slot) {
        return effects.getOrDefault(slot, List.of());
    }

    public void addEffect(EffectSlot slot, CardEffect effect) {
        effects.computeIfAbsent(slot, k -> new ArrayList<>()).add(effect);
    }

    public void addActivatedAbility(ActivatedAbility ability) {
        activatedAbilities.add(ability);
    }

    public boolean isAura() {
        return subtypes.contains(CardSubtype.AURA);
    }

    public boolean isNeedsDamageDistribution() {
        boolean inSpell = getEffects(EffectSlot.SPELL).stream()
                .anyMatch(e -> e instanceof DealXDamageDividedAmongTargetAttackingCreaturesEffect);
        boolean inAbility = activatedAbilities.stream()
                .flatMap(a -> a.getEffects().stream())
                .anyMatch(e -> e instanceof DealXDamageDividedAmongTargetAttackingCreaturesEffect);
        return inSpell || inAbility;
    }
}
