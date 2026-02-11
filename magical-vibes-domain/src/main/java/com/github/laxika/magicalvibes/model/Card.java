package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public class Card {

    private final String name;
    private final CardType type;
    private final String manaCost;
    private final CardColor color;

    @Setter private List<CardSubtype> subtypes = List.of();
    @Setter private String cardText;
    @Setter private List<CardEffect> onTapEffects = List.of();
    @Setter private Integer power;
    @Setter private Integer toughness;
    @Setter private Set<Keyword> keywords = Set.of();
    @Setter private List<CardEffect> onEnterBattlefieldEffects = List.of();
    @Setter private List<CardEffect> spellEffects = List.of();
    @Setter private List<CardEffect> onAllyCreatureEntersBattlefieldEffects = List.of();
    @Setter private List<CardEffect> staticEffects = List.of();
    @Setter private List<CardEffect> onSacrificeEffects = List.of();
    @Setter private List<CardEffect> tapActivatedAbilityEffects = List.of();
    @Setter private String tapActivatedAbilityCost;
    @Setter private boolean needsTarget;
    @Setter private String setCode;
    @Setter private String collectorNumber;
    @Setter private String flavorText;

    public boolean isAura() {
        return subtypes.contains(CardSubtype.AURA);
    }

    public boolean isNeedsDamageDistribution() {
        return Stream.of(spellEffects, tapActivatedAbilityEffects)
                .flatMap(List::stream)
                .anyMatch(e -> e instanceof DealXDamageDividedAmongTargetAttackingCreaturesEffect);
    }
}
