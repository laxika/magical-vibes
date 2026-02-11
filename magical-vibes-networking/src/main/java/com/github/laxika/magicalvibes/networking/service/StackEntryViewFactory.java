package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.networking.model.StackEntryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StackEntryViewFactory {

    private final CardViewFactory cardViewFactory;

    public StackEntryView create(StackEntry entry) {
        return new StackEntryView(
                entry.getEntryType(),
                cardViewFactory.create(entry.getCard()),
                entry.getControllerId(),
                entry.getDescription()
        );
    }
}
