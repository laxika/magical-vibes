package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermanentViewFactory {

    private final CardViewFactory cardViewFactory;

    public PermanentView create(Permanent p, int bonusPower, int bonusToughness, Set<Keyword> bonusKeywords, boolean animatedCreature) {
        Set<Keyword> allKeywords = new HashSet<>(p.getGrantedKeywords());
        allKeywords.addAll(bonusKeywords);
        CardView cardView = cardViewFactory.create(p.getCard());
        cardView = applyTextReplacements(cardView, p);
        cardView = applyGrantedSubtypes(cardView, p);
        return new PermanentView(
                p.getId(), cardView,
                p.isTapped(), p.isAttacking(), p.isBlocking(),
                new ArrayList<>(p.getBlockingTargets()), p.isSummoningSick(),
                p.getPowerModifier() + bonusPower,
                p.getToughnessModifier() + bonusToughness,
                allKeywords,
                p.getEffectivePower() + bonusPower,
                p.getEffectiveToughness() + bonusToughness,
                p.getAttachedTo(),
                p.getChosenColor(),
                p.getRegenerationShield(),
                p.isCantBeBlocked(),
                animatedCreature,
                p.getLoyaltyCounters()
        );
    }

    private CardView applyGrantedSubtypes(CardView cardView, Permanent p) {
        if (p.getGrantedSubtypes().isEmpty()) {
            return cardView;
        }
        List<CardSubtype> mergedSubtypes = new ArrayList<>(cardView.subtypes());
        for (CardSubtype subtype : p.getGrantedSubtypes()) {
            if (!mergedSubtypes.contains(subtype)) {
                mergedSubtypes.add(subtype);
            }
        }
        return new CardView(
                cardView.name(), cardView.type(), cardView.supertypes(), mergedSubtypes,
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.targetsPlayer(),
                cardView.requiresAttackingTarget(), cardView.allowedTargetTypes(),
                cardView.activatedAbilities(), cardView.loyalty(),
                cardView.minTargets(), cardView.maxTargets(), cardView.hasConvoke()
        );
    }

    private CardView applyTextReplacements(CardView cardView, Permanent p) {
        if (p.getTextReplacements().isEmpty() || cardView.cardText() == null) {
            return cardView;
        }
        String modifiedText = cardView.cardText();
        for (TextReplacement rep : p.getTextReplacements()) {
            modifiedText = modifiedText.replace(rep.fromWord(), rep.toWord());
        }
        return new CardView(
                cardView.name(), cardView.type(), cardView.supertypes(), cardView.subtypes(),
                modifiedText, cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.targetsPlayer(),
                cardView.requiresAttackingTarget(), cardView.allowedTargetTypes(),
                cardView.activatedAbilities(), cardView.loyalty(),
                cardView.minTargets(), cardView.maxTargets(), cardView.hasConvoke()
        );
    }
}
