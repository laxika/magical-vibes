package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermanentViewFactory {

    private final CardViewFactory cardViewFactory;

    public PermanentView create(Permanent p, int bonusPower, int bonusToughness, Set<Keyword> bonusKeywords) {
        Set<Keyword> allKeywords = new HashSet<>(p.getGrantedKeywords());
        allKeywords.addAll(bonusKeywords);
        return new PermanentView(
                p.getId(), cardViewFactory.create(p.getCard()),
                p.isTapped(), p.isAttacking(), p.isBlocking(),
                new ArrayList<>(p.getBlockingTargets()), p.isSummoningSick(),
                p.getPowerModifier() + bonusPower,
                p.getToughnessModifier() + bonusToughness,
                allKeywords,
                p.getEffectivePower() + bonusPower,
                p.getEffectiveToughness() + bonusToughness,
                p.getAttachedTo(),
                p.getChosenColor()
        );
    }
}
